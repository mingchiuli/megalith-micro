package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.handler.impl.RedisCacheEvictHandler;
import wiki.chiu.micro.cache.listener.RedisCacheEvictMessageListener;


@AutoConfiguration
@ConditionalOnMissingClass("org.springframework.amqp.rabbit.connection.ConnectionFactory")
public class CacheEvictRedisConfig {

    private static final String CACHE_EVICT_TOPIC = "cacheRedisEvictTopic";

    private final RedissonClient redissonClient;

    private final Cache<@NonNull String, Object> localCache;

    private final JsonMapper jsonMapper;

    public CacheEvictRedisConfig(@Qualifier("cacheRedissonClient") RedissonClient redissonClient, @Qualifier("caffeineCache") Cache<@NonNull String, Object> localCache, JsonMapper jsonMapper) {
        this.redissonClient = redissonClient;
        this.localCache = localCache;
        this.jsonMapper = jsonMapper;
    }

    @PostConstruct
    private void init() {
        var listener = new RedisCacheEvictMessageListener(CACHE_EVICT_TOPIC, redissonClient, jsonMapper, localCache);
        listener.initListener();
    }

    @Bean
    CacheEvictHandler redisCacheEvictHandler() {
        return new RedisCacheEvictHandler(redissonClient, jsonMapper, CACHE_EVICT_TOPIC);
    }
}
