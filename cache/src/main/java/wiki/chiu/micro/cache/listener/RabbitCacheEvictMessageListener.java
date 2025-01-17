package wiki.chiu.micro.cache.listener;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.Set;

public class RabbitCacheEvictMessageListener {

    private final Cache<String, Object> localCache;

    public RabbitCacheEvictMessageListener(Cache<String, Object> localCache) {
        this.localCache = localCache;
    }

    public void handleMessage(Set<String> keys) {
        localCache.invalidateAll(keys);
    }
}
