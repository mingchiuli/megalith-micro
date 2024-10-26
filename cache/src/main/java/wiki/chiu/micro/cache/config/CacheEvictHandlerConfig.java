package wiki.chiu.micro.cache.config;

import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CacheEvictHandlerConfig {

    @Value("${megalith.cache.fanout-exchange}")
    public String CACHE_EVICT_FANOUT_EXCHANGE;

    private final RedissonClient redissonClient;

    private final AmqpTemplate amqpTemplate;

    public CacheEvictHandlerConfig(RedissonClient redissonClient, AmqpTemplate amqpTemplate) {
        this.redissonClient = redissonClient;
        this.amqpTemplate = amqpTemplate;
    }

    @Bean
    CacheEvictHandler cacheEvictRabbitHandler() {
        return new CacheEvictHandler(CACHE_EVICT_FANOUT_EXCHANGE, redissonClient, amqpTemplate);
    }

}
