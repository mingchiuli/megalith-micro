package wiki.chiu.micro.exhibit.schedule;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static wiki.chiu.micro.common.lang.Const.*;

/**
 * @author mingchiuli
 * @create 2022-11-29 10:52 pm
 */
@Component
public class BlogSchedule {

    @Qualifier("commonExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;

    private final RedissonClient redissonClient;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public BlogSchedule(@Qualifier("commonExecutor") ThreadPoolTaskExecutor taskExecutor, RedissonClient redissonClient) {
        this.taskExecutor = taskExecutor;
        this.redissonClient = redissonClient;
    }

    private void buildExecutor(Runnable task, String key, String finishKey) {
        RLock rLock = redissonClient.getLock(key);
        if (!rLock.tryLock()) {
            return;
        }

        try {
            boolean executed = redissonClient.getBucket(finishKey).isExists();
            if (!executed) {
                CompletableFuture.runAsync(task, taskExecutor.getThreadPoolExecutor());
                redissonClient.getBucket(finishKey).set("flag", Duration.ofSeconds(10));
            }
        } finally {
            rLock.unlock();
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void statisticExec() {

        String statisticsKey = "statisticsKey";
        String statisticsFinishKey = "statisticsFinishKey";

        buildExecutor(() -> {
            var now = LocalDateTime.now();

            int hourOfDay = now.getHour();
            int dayOfWeek = now.getDayOfWeek().getValue();
            int dayOfMonth = now.getDayOfMonth();
            int dayOfYear = now.getDayOfYear();

            if (hourOfDay == 0) {
                redissonClient.getBucket(DAY_VISIT).delete();
                if (dayOfWeek == 1) {
                    redissonClient.getBucket(WEEK_VISIT).delete();
                    redissonClient.getBucket(HOT_READ).unlink();
                }
                if (dayOfMonth == 1) {
                    redissonClient.getBucket(MONTH_VISIT).delete();
                }
                if (dayOfYear == 1) {
                    redissonClient.getBucket(YEAR_VISIT).delete();
                }
            }
        }, statisticsKey, statisticsFinishKey);
    }
}
