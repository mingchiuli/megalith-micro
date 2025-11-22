package wiki.chiu.micro.cache.handler.impl;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;

import java.util.HashSet;


public class RedisCacheEvictHandler extends CacheEvictHandler {

    private final JsonMapper jsonMapper;

    private final String CACHE_EVICT_TOPIC;

    public RedisCacheEvictHandler(@Qualifier("cacheRedissonClient") RedissonClient redissonClient, JsonMapper jsonMapper, String CACHE_EVICT_TOPIC) {
        super(redissonClient);
        this.jsonMapper = jsonMapper;
        this.CACHE_EVICT_TOPIC = CACHE_EVICT_TOPIC;
    }

    @Override
    public void evictCache(HashSet<String> keys) {
        redissonClient.getKeys().delete(keys.toArray(new String[0]));
        String value = jsonMapper.writeValueAsString(keys);
        redissonClient.getReliableTopic(CACHE_EVICT_TOPIC).publish(value);
    }

}