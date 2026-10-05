package wiki.chiu.micro.exhibit.adapter.out.redis;

import static wiki.chiu.micro.common.constant.Const.HOT_READ;
import static wiki.chiu.micro.common.constant.Const.READ_TOKEN;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;

@Component
public class RedisBlogReadStateStore implements BlogReadStateStore {

    private final RedissonClient redissonClient;

    public RedisBlogReadStateStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void clearReadToken(Long blogId) {
        redissonClient.getKeys().delete(READ_TOKEN + blogId);
    }

    @Override
    public void removeFromHotRead(Long blogId) {
        redissonClient.getScoredSortedSet(HOT_READ).remove(blogId.toString());
    }
}
