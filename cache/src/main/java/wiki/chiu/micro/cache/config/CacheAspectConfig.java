package wiki.chiu.micro.cache.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;

import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.cache.aspect.CacheAspect;

import org.redisson.api.RedissonClient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;


/**
 * 统一缓存处理
 *
 * @author mingchiuli
 * order: 多个切面执行顺序，越小越先执行
 * @since 2021-12-01 7:48 AM
 */
@AutoConfiguration
public class CacheAspectConfig {

    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper;

    private final com.github.benmanes.caffeine.cache.Cache<String, Object> localCache;

    public CacheAspectConfig(@Qualifier("cacheRedissonClient") RedissonClient redissonClient, ObjectMapper objectMapper, Cache<String, Object> localCache, CommonCacheKeyGenerator commonCacheKeyGenerator) {
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
        this.localCache = localCache;
    }

    @Bean
    CacheAspect cacheAspect() {
        return new CacheAspect(redissonClient, objectMapper, commonCacheKeyGenerator(), localCache);
    }


    @Bean
    CommonCacheKeyGenerator commonCacheKeyGenerator() {
        return new CommonCacheKeyGenerator(objectMapper);
    }
}
