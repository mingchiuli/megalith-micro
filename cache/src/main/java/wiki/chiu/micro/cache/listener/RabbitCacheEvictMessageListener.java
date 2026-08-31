package wiki.chiu.micro.cache.listener;

import com.github.benmanes.caffeine.cache.Cache;

import org.jspecify.annotations.NonNull;

import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

public record RabbitCacheEvictMessageListener(
    Cache<@NonNull String, LocalCacheEntry> localCache) {

    public void handleMessage(CacheEvictionMessage message) {
        localCache.invalidateAll(message.keys());
    }
}
