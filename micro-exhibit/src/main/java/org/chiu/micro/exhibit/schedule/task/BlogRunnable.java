package org.chiu.micro.exhibit.schedule.task;

import org.chiu.micro.exhibit.service.BlogService;
import org.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Optional;

import static org.chiu.micro.exhibit.lang.Const.BLOOM_FILTER_BLOG;

/**
 * @author mingchiuli
 * @create 2023-06-24 5:00 pm
 */
public record BlogRunnable(
        BlogService blogService,
        BlogWrapper blogWrapper,
        BlogSensitiveWrapper blogSensitiveWrapper,
        RedissonClient redissonClient,
        Integer pageNo, Integer pageSize) implements Runnable {

    @Override
    public void run() {
        List<Long> idList = blogService.findIds(pageNo, pageSize);
        Optional.ofNullable(idList).ifPresent(ids ->
                ids.forEach(id -> {
                    redissonClient.getBitSet(BLOOM_FILTER_BLOG.getInfo()).set(id, true);
                    Integer status = blogWrapper.findStatusById(id);
                    blogWrapper.findById(id);
                    blogSensitiveWrapper.findSensitiveByBlogId(id);
                })
        );
    }
}
