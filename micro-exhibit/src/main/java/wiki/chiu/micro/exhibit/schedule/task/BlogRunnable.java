package wiki.chiu.micro.exhibit.schedule.task;

import wiki.chiu.micro.exhibit.service.BlogService;
import wiki.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Optional;

import static wiki.chiu.micro.common.lang.Const.BLOOM_FILTER_BLOG;

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
                    redissonClient.getBitSet(BLOOM_FILTER_BLOG).set(id, true);
                    blogWrapper.findStatusById(id);
                    blogWrapper.findById(id);
                    blogSensitiveWrapper.findSensitiveByBlogId(id);
                })
        );
    }
}
