package org.chiu.micro.exhibit.schedule.task;

import org.chiu.micro.exhibit.lang.StatusEnum;
import org.chiu.micro.exhibit.service.BlogService;
import org.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Optional;

import static org.chiu.micro.exhibit.lang.Const.BLOOM_FILTER_BLOG;

/**
 * @author mingchiuli
 * @create 2023-06-24 5:00 pm
 */
public record BlogRunnable (
        BlogService blogService,
        BlogWrapper blogWrapper,
        BlogSensitiveWrapper blogSensitiveWrapper,
        StringRedisTemplate redisTemplate,
        Integer pageNo, Integer pageSize) implements Runnable {

    @Override
    public void run() {
        List<Long> idList = blogService.findIds(pageNo, pageSize);
        Optional.ofNullable(idList).ifPresent(ids ->
                ids.forEach(id -> {
                    redisTemplate.opsForValue().setBit(BLOOM_FILTER_BLOG.getInfo(), id, true);
                    Integer status = blogWrapper.findStatusById(id);
                    blogWrapper.findById(id);
                    if (StatusEnum.SENSITIVE_FILTER.getCode().equals(status)) {
                        blogSensitiveWrapper.findSensitiveByBlogId(id);
                    }
                })
        );
    }
}
