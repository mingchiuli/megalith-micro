package wiki.chiu.micro.cache.listener;

import com.github.benmanes.caffeine.cache.Cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.jspecify.annotations.NonNull;
import org.redisson.api.RReliableTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

public final class RedisCacheEvictMessageListener {

    private final RReliableTopic reliableTopic;
    private final JsonMapper jsonMapper;
    private final Cache<@NonNull String, LocalCacheEntry> localCache;
    private String listenerId;

    public RedisCacheEvictMessageListener(
        String topic,
        RedissonClient redissonClient,
        JsonMapper jsonMapper,
        Cache<@NonNull String, LocalCacheEntry> localCache) {
        this.reliableTopic = redissonClient.getReliableTopic(topic, StringCodec.INSTANCE);
        this.jsonMapper = jsonMapper;
        this.localCache = localCache;
    }

    @PostConstruct
    void start() {
        listenerId =
            reliableTopic.addListener(
                String.class,
                (_, message) -> {
                    CacheEvictionMessage eviction =
                        jsonMapper.readValue(message, CacheEvictionMessage.class);
                    localCache.invalidateAll(eviction.keys());
                });
    }

    @PreDestroy
    void stop() {
        if (listenerId != null) {
            reliableTopic.removeListener(listenerId);
        }
    }
}
