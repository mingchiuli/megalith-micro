package org.chiu.micro.exhibit.cache.mq;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CacheBlogEvictMessageListener {

    private final Cache<String, Object> localCache;

    @SneakyThrows
    public void handleMessage(Set<String> keys) {
        localCache.invalidateAll(keys);
    }
}
