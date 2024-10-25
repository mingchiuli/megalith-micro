package org.chiu.micro.cache.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;

import org.chiu.micro.cache.aspect.CacheAspect;
import org.chiu.micro.cache.utils.CommonCacheKeyGenerator;

import org.redisson.api.RedissonClient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;


/**
 * 统一缓存处理
 *
 * @author mingchiuli
 * order: 多个切面执行顺序，越小越先执行
 * @create 2021-12-01 7:48 AM
 */
@AutoConfiguration
public class CacheAspectConfig {

    private final RedissonClient redissonClient;

    private final CommonCacheKeyGenerator commonCacheKeyGenerator;

    private final ObjectMapper objectMapper;

    private final RedissonClient redisson;

    private final com.github.benmanes.caffeine.cache.Cache<String, Object> localCache;

    public CacheAspectConfig(RedissonClient redissonClient, CommonCacheKeyGenerator commonCacheKeyGenerator, ObjectMapper objectMapper, RedissonClient redisson, Cache<String, Object> localCache) {
        this.redissonClient = redissonClient;
        this.commonCacheKeyGenerator = commonCacheKeyGenerator;
        this.objectMapper = objectMapper;
        this.redisson = redisson;
        this.localCache = localCache;
    }

    @Bean
    CacheAspect cacheAspect() {
        return new CacheAspect(redissonClient, objectMapper, commonCacheKeyGenerator, redisson, localCache);
    }
}
