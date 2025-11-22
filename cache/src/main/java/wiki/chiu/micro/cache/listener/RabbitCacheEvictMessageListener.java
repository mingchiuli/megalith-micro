package wiki.chiu.micro.cache.listener;

import com.github.benmanes.caffeine.cache.Cache;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public record RabbitCacheEvictMessageListener(Cache<@NonNull String, Object> localCache) {

    public void handleMessage(Set<String> keys) {
        localCache.invalidateAll(keys);
    }
}
