package wiki.chiu.micro.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.handler.impl.RedisCacheEvictHandler;
import wiki.chiu.micro.cache.listener.RedisCacheEvictMessageListener;


@AutoConfiguration
@ConditionalOnProperty(prefix = "megalith", name = "cache.topic", havingValue = "redis", matchIfMissing = true)
public class CacheEvictRedisQueueConfig {

    private static final String CACHE_EVICT_TOPIC = "cacheRedisEvictTopic";

    private final RedissonClient redissonClient;

    private final Cache<String, Object> localCache;

    private final ObjectMapper objectMapper;

    public CacheEvictRedisQueueConfig(@Qualifier("cacheRedissonClient") RedissonClient redissonClient, @Qualifier("caffeineCache") Cache<String, Object> localCache, ObjectMapper objectMapper) {
        this.redissonClient = redissonClient;
        this.localCache = localCache;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() {
        var listener = new RedisCacheEvictMessageListener(CACHE_EVICT_TOPIC, redissonClient, objectMapper, localCache);
        listener.initListener();
    }

    @Bean
    CacheEvictHandler redisCacheEvictHandler() {
        return new RedisCacheEvictHandler(redissonClient, objectMapper, CACHE_EVICT_TOPIC);
    }
}
