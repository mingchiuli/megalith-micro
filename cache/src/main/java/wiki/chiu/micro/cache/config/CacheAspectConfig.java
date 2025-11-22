package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.cache.aspect.CacheAspect;



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

    private final JsonMapper jsonMapper;

    private final com.github.benmanes.caffeine.cache.Cache<@NonNull String, Object> localCache;

    private final com.github.benmanes.caffeine.cache.Cache<@NonNull String, ReentrantLock> localLockMap;

    public CacheAspectConfig(@Qualifier("cacheRedissonClient") RedissonClient redissonClient, JsonMapper jsonMapper, @Qualifier("caffeineCache") Cache<@NonNull String, Object> localCache, @Qualifier("localLockMap") Cache<@NonNull String, ReentrantLock> localLockMap) {
        this.redissonClient = redissonClient;
        this.jsonMapper = jsonMapper;
        this.localCache = localCache;
        this.localLockMap = localLockMap;
    }

    @Bean
    CacheAspect cacheAspect() {
        return new CacheAspect(redissonClient, jsonMapper, commonCacheKeyGenerator(), localCache, localLockMap);
    }


    @Bean
    CommonCacheKeyGenerator commonCacheKeyGenerator() {
        return new CommonCacheKeyGenerator(jsonMapper);
    }
}
