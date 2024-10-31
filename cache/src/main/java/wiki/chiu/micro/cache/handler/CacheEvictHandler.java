package wiki.chiu.micro.cache.handler;

import org.redisson.api.RedissonClient;

import java.util.Set;

public abstract class CacheEvictHandler {

    protected final RedissonClient redissonClient;

    protected CacheEvictHandler(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public abstract void evictCache(Set<String> keys);

}
