package wiki.chiu.micro.cache.listener;

import com.github.benmanes.caffeine.cache.Cache;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

public record RedisCacheEvictMessageListener(String CACHE_EVICT_TOPIC, RedissonClient redissonClient,
                                             JsonMapper jsonMapper, Cache<@NonNull String, Object> localCache) {

    private static final TypeReference<Set<String>> type = new TypeReference<>() {
    };

    public void initListener() {
        redissonClient.getReliableTopic(CACHE_EVICT_TOPIC).addListener(String.class, (_, message) -> {
            Set<String> keys = jsonMapper.readValue(message, type);
            localCache.invalidateAll(keys);
        });
    }
}
