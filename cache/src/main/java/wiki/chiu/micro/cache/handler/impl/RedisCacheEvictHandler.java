package wiki.chiu.micro.cache.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.redisson.api.RedissonClient;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;

import java.util.Set;


public class RedisCacheEvictHandler extends CacheEvictHandler {

    private final ObjectMapper objectMapper;

    private final String CACHE_EVICT_TOPIC;

    public RedisCacheEvictHandler(RedissonClient redissonClient, ObjectMapper objectMapper, String CACHE_EVICT_TOPIC) {
        super(redissonClient);
        this.objectMapper = objectMapper;
        this.CACHE_EVICT_TOPIC = CACHE_EVICT_TOPIC;
    }

    @Override
    public void evictCache(Set<String> keys) {
        redissonClient.getKeys().delete(keys.toArray(new String[0]));
        try {
            String value = objectMapper.writeValueAsString(keys);
            redissonClient.getReliableTopic(CACHE_EVICT_TOPIC).publish(value);
        } catch (JsonProcessingException _) {}
    }

}
