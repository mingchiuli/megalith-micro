package org.chiu.micro.exhibit.schedule;

import org.chiu.micro.exhibit.service.BlogService;
import org.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.chiu.micro.exhibit.schedule.task.BlogRunnable;
import org.chiu.micro.exhibit.schedule.task.BlogsRunnable;
import lombok.RequiredArgsConstructor;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.chiu.micro.exhibit.lang.Const.*;

/**
 * @author mingchiuli
 * @create 2022-11-29 10:52 pm
 */
@Component
@RequiredArgsConstructor
public class CacheSchedule {

    @Qualifier("commonExecutor")
    private final ExecutorService taskExecutor;

    private final BlogService blogService;

    private final BlogWrapper blogWrapper;

    private final StringRedisTemplate redisTemplate;

    private final RedissonClient redisson;

    private final BlogSensitiveWrapper blogSensitiveWrapper;

    @Value("${blog.blog-page-size}")
    private int blogPageSize;

    private static final String CACHE_FINISH_FLAG = "cache_finish_flag";

    private static final String CACHE_KEY = "cacheKey";

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void configureTask() {

        RLock rLock = redisson.getLock(CACHE_KEY);
        if (Boolean.FALSE.equals(rLock.tryLock())) {
            return;
        }

        try {
            Boolean executed = redisTemplate.hasKey(CACHE_FINISH_FLAG);
            if (Boolean.FALSE.equals(executed)) {
                exec();
                redisTemplate.opsForValue().set(CACHE_FINISH_FLAG, "flag", 60, TimeUnit.SECONDS);
            }
        } finally {
            rLock.unlock();
        }
    }

    private void exec() {
        List<Integer> years = blogService.getYears();
        Long total = blogService.count();
        // getBlogDetail和getBlogStatus接口 分别考虑缓存和bloom
        blogRunnableExec(total);

        // listPage接口 分别考虑缓存和bloom
        blogsRunnableExec(total);

        // listByYear接口 分别考虑缓存和bloom
        listExec(years);

        // searchYears和getCountByYear
        yearExec(years);

        //del statistic & del hot read
        statisticExec();
    }

    private void blogRunnableExec(Long total) {
         // getBlogDetail和getBlogStatus接口 分别考虑缓存和bloom
        CompletableFuture.runAsync(() -> {
            int pageSize = 20;
            int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
            for (int i = 1; i <= totalPage; i++) {
                var runnable = new BlogRunnable(blogService, blogWrapper, blogSensitiveWrapper, redisTemplate, i, pageSize);
                taskExecutor.execute(runnable);
            }
        }, taskExecutor);
    }

    private void blogsRunnableExec(Long total) {
        CompletableFuture.runAsync(() -> {
            int totalPage = (int) (total % blogPageSize == 0 ?
                    total / blogPageSize :
                    total / blogPageSize + 1);
            for (int i = 1; i <= totalPage; i++) {
                var runnable = new BlogsRunnable(redisTemplate, blogService, i);
                taskExecutor.execute(runnable);
            }
        }, taskExecutor);
    }

    private void listExec(List<Integer> years) {
        CompletableFuture.runAsync(() -> {
            
            for (Integer year : years) {
                // 当前年份的总页数
                taskExecutor.execute(() -> {
                    long countByYear = blogService.getCountByYear(year);
                    int totalPage = (int) (countByYear % blogPageSize == 0 ?
                                                countByYear / blogPageSize :
                                                countByYear / blogPageSize + 1);

                    for (int no = 1; no <= totalPage; no++) {
                        redisTemplate.opsForValue().setBit(BLOOM_FILTER_YEAR_PAGE.getInfo() + year, no, true);
                        blogService.findPage(no, year);
                    }
                });
            }

        }, taskExecutor);
    }

    private void yearExec(List<Integer> years) {
        CompletableFuture.runAsync(() -> years.forEach(year -> {
            redisTemplate.opsForValue().setBit(BLOOM_FILTER_YEARS.getInfo(), year, true);
            blogService.getCountByYear(year);
        }), taskExecutor);
    }

    private void statisticExec() {
        CompletableFuture.runAsync(() -> {
            var now = LocalDateTime.now();

            int hourOfDay = now.getHour();
            int dayOfWeek = now.getDayOfWeek().getValue();
            int dayOfMonth = now.getDayOfMonth();
            int dayOfYear = now.getDayOfYear();

            if (hourOfDay == 0) {
                redisTemplate.delete(DAY_VISIT.getInfo());
                if (dayOfWeek == 1) {
                    redisTemplate.delete(WEEK_VISIT.getInfo());
                    redisTemplate.unlink(HOT_READ.getInfo());
                }
                if (dayOfMonth == 1) {
                    redisTemplate.delete(MONTH_VISIT.getInfo());
                }
                if (dayOfYear == 1) {
                    redisTemplate.delete(YEAR_VISIT.getInfo());
                }
            }
        }, taskExecutor);
    }
}
