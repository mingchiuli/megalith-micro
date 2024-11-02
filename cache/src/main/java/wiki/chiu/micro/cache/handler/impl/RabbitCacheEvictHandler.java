package wiki.chiu.micro.cache.handler.impl;

import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;


import java.util.Set;

public class RabbitCacheEvictHandler extends CacheEvictHandler {

    private final String CACHE_EVICT_FANOUT_EXCHANGE;

    private final RabbitTemplate rabbitTemplate;

    public RabbitCacheEvictHandler(@Qualifier("cacheRabbitTemplate") RabbitTemplate rabbitTemplate, @Qualifier("cacheRedissonClient") RedissonClient redissonClient, String CACHE_EVICT_FANOUT_EXCHANGE) {
        super(redissonClient);
        this.rabbitTemplate = rabbitTemplate;
        this.CACHE_EVICT_FANOUT_EXCHANGE = CACHE_EVICT_FANOUT_EXCHANGE;
    }

    @Override
    public void evictCache(Set<String> keys) {
        redissonClient.getKeys().delete(keys.toArray(new String[0]));
        rabbitTemplate.convertAndSend(CACHE_EVICT_FANOUT_EXCHANGE, "", keys);
    }
}
