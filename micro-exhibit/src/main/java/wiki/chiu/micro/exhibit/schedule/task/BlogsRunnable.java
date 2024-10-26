package wiki.chiu.micro.exhibit.schedule.task;

import wiki.chiu.micro.exhibit.service.BlogService;
import org.redisson.api.RedissonClient;

import static wiki.chiu.micro.common.lang.Const.BLOOM_FILTER_PAGE;


/**
 * @author mingchiuli
 * @create 2023-06-24 5:32 pm
 */
public record BlogsRunnable(
        RedissonClient redissonClient,
        BlogService blogService,
        Integer pageNo) implements Runnable {

    @Override
    public void run() {
        redissonClient.getBitSet(BLOOM_FILTER_PAGE).set(pageNo, true);
        blogService.findPage(pageNo, Integer.MIN_VALUE);
    }
}
