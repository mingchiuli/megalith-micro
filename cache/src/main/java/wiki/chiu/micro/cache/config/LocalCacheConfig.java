package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import wiki.chiu.micro.cache.store.LocalCacheEntry;

@AutoConfiguration
public class LocalCacheConfig {

    @Bean("caffeineCache")
    Cache<@NonNull String, LocalCacheEntry> caffeineCache(CacheProperties properties) {
        return Caffeine.newBuilder()
            .initialCapacity(properties.getLocal().getInitialCapacity())
            .maximumSize(properties.getLocal().getMaximumEntries())
            .expireAfter(
                Expiry.<String, LocalCacheEntry>creating((_, entry) -> entry.ttl()))
            .build();
    }

    @Bean("localLockMap")
    Cache<@NonNull String, ReentrantLock> localLockMap(CacheProperties properties) {
        return Caffeine.newBuilder()
            .initialCapacity(properties.getLocal().getInitialCapacity())
            .maximumSize(properties.getLocal().getMaximumEntries())
            .expireAfterAccess(properties.getSingleFlight().getLockEntryTtl())
            .build();
    }
}
