package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.aspect.CacheAspect;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.cache.key.impl.JacksonCacheKeyFactory;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAspectConfig {

    @Bean
    static CacheContractValidator cacheContractValidator() {
        return new CacheContractValidator();
    }

    @Bean
    CacheMetrics cacheMetrics(ObjectProvider<@NonNull MeterRegistry> meterRegistries) {
        return new CacheMetrics(meterRegistries.getIfAvailable());
    }

    @Bean
    CacheKeyFactory cacheKeyFactory(JsonMapper jsonMapper) {
        return new JacksonCacheKeyFactory(jsonMapper);
    }

    @Bean
    CacheAspect cacheAspect(
        RedissonClient redissonClient,
        JsonMapper jsonMapper,
        CacheKeyFactory cacheKeyFactory,
        @Qualifier("caffeineCache") Cache<@NonNull String, LocalCacheEntry> localCache,
        @Qualifier("localLockMap") Cache<@NonNull String, ReentrantLock> localLockMap,
        CacheProperties properties,
        CacheMetrics metrics) {
        properties.validate();
        return new CacheAspect(
            redissonClient,
            jsonMapper,
            cacheKeyFactory,
            localCache,
            localLockMap,
            properties,
            metrics);
    }
}
