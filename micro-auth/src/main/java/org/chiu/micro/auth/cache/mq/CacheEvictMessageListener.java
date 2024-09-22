package org.chiu.micro.auth.cache.mq;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CacheEvictMessageListener {

    private final Cache<String, Object> localCache;

    public CacheEvictMessageListener(Cache<String, Object> localCache) {
        this.localCache = localCache;
    }

    public void handleMessage(Set<String> keys) {
        localCache.invalidateAll(keys);
    }
}
