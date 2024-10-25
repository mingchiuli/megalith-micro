package org.chiu.micro.cache.handler;

import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.Set;

public class CacheEvictHandler {

    private final String CACHE_EVICT_FANOUT_EXCHANGE;

    private final RedissonClient redissonClient;

    private final AmqpTemplate amqpTemplate;

    public CacheEvictHandler(String CACHE_EVICT_FANOUT_EXCHANGE, RedissonClient redissonClient, AmqpTemplate amqpTemplate) {
        this.CACHE_EVICT_FANOUT_EXCHANGE = CACHE_EVICT_FANOUT_EXCHANGE;
        this.redissonClient = redissonClient;
        this.amqpTemplate = amqpTemplate;
    }

    public void evictCache(Set<String> keys, Set<String> localKeys) {
        redissonClient.getKeys().delete(keys.toArray(new String[0]));
        amqpTemplate.convertAndSend(CACHE_EVICT_FANOUT_EXCHANGE, "", localKeys);
    }
}
