package wiki.chiu.micro.cache.adapter.out.eviction;

import com.github.benmanes.caffeine.cache.Cache;

import java.time.Duration;

import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.application.model.CacheEvictionMessage;
import wiki.chiu.micro.cache.application.model.LocalCacheEntry;
import wiki.chiu.micro.cache.application.CacheMetrics;

public final class RedisCacheEvictor extends AbstractCacheEvictor {

    private final RedissonClient redissonClient;
    private final JsonMapper jsonMapper;
    private final String topic;

    public RedisCacheEvictor(
        RedissonClient redissonClient,
        JsonMapper jsonMapper,
        Cache<@NonNull String, LocalCacheEntry> localCache,
        Duration singleFlightWaitTimeout,
        String topic,
        CacheMetrics metrics) {
        super(redissonClient, localCache, singleFlightWaitTimeout, metrics);
        this.redissonClient = redissonClient;
        this.jsonMapper = jsonMapper;
        this.topic = topic;
    }

    @Override
    protected String transport() {
        return "redis";
    }

    @Override
    protected void broadcast(CacheEvictionMessage message) {
        String value = jsonMapper.writeValueAsString(message);
        redissonClient.getReliableTopic(topic, StringCodec.INSTANCE).publish(value);
    }
}
