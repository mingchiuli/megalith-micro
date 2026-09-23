package wiki.chiu.micro.cache.adapter.in.messaging;

import com.github.benmanes.caffeine.cache.Cache;

import org.jspecify.annotations.NonNull;

import wiki.chiu.micro.cache.application.model.CacheEvictionMessage;
import wiki.chiu.micro.cache.application.model.LocalCacheEntry;

public record RabbitCacheEvictMessageListener(
    Cache<@NonNull String, LocalCacheEntry> localCache) {

    public void handleMessage(CacheEvictionMessage message) {
        localCache.invalidateAll(message.keys());
    }
}
