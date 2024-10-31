package wiki.chiu.micro.cache.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import org.redisson.api.RedissonClient;

import java.util.Set;

public class RedisCacheEvictMessageListener {

    private static final TypeReference<Set<String>> type = new TypeReference<>() {};

    private final String CACHE_EVICT_TOPIC;

    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper;

    private final Cache<String, Object> localCache;

    public RedisCacheEvictMessageListener(String CACHE_EVICT_TOPIC, RedissonClient redissonClient, ObjectMapper objectMapper, Cache<String, Object> localCache) {
        this.CACHE_EVICT_TOPIC = CACHE_EVICT_TOPIC;
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
        this.localCache = localCache;
    }

    public void initListener() {
        redissonClient.getReliableTopic(CACHE_EVICT_TOPIC).addListener(String.class, (_, message) -> {
            try {
                Set<String> keys = objectMapper.readValue(message, type);
                localCache.invalidateAll(keys);
            } catch (JsonProcessingException _) {
            }
        });
    }
}
