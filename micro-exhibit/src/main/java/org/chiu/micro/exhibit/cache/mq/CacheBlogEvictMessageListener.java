package org.chiu.micro.exhibit.cache.mq;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CacheBlogEvictMessageListener {

    private final Cache<String, Object> localCache;

    public CacheBlogEvictMessageListener(Cache<String, Object> localCache) {
        this.localCache = localCache;
    }

    public void handleMessage(Set<String> keys) {
        localCache.invalidateAll(keys);
    }
}
