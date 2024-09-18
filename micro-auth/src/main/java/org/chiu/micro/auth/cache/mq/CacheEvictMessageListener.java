package org.chiu.micro.auth.cache.mq;

import java.util.Set;

import org.springframework.stereotype.Component;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CacheEvictMessageListener {

    private final Cache<String, Object> localCache;

    public void handleMessage(Set<String> keys) {
        localCache.invalidateAll(keys);
    }
}
