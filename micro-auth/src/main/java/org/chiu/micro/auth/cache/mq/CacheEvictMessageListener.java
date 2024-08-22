package org.chiu.micro.auth.cache.mq;

import java.util.Set;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CacheEvictMessageListener {

    private final Cache<String, Object> localCache;

    @SneakyThrows
    public void handleMessage(Set<String> keys) {
        localCache.invalidateAll(keys);
    }
}
