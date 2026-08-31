package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;

import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.handler.impl.RedisCacheEvictor;
import wiki.chiu.micro.cache.listener.RedisCacheEvictMessageListener;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

@AutoConfiguration
@AutoConfigureAfter(CacheEvictRabbitConfig.class)
@Conditional(CacheRedisTransportCondition.class)
@ConditionalOnMissingBean(CacheEvictor.class)
public class CacheEvictRedisConfig {

    @Bean
    RedisCacheEvictMessageListener redisCacheEvictMessageListener(
        RedissonClient redissonClient,
        JsonMapper jsonMapper,
        @Qualifier("caffeineCache") Cache<@NonNull String, LocalCacheEntry> localCache,
        CacheProperties properties) {
        String topic = properties.getEviction().getRedis().getTopic();
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException(
                "megalith.cache.eviction.redis.topic must not be blank");
        }
        return new RedisCacheEvictMessageListener(topic, redissonClient, jsonMapper, localCache);
    }

    @Bean
    CacheEvictor redisCacheEvictor(
        RedissonClient redissonClient,
        JsonMapper jsonMapper,
        @Qualifier("caffeineCache") Cache<@NonNull String, LocalCacheEntry> localCache,
        CacheProperties properties,
        CacheMetrics metrics) {
        return new RedisCacheEvictor(redissonClient, jsonMapper, localCache, properties, metrics);
    }
}
