package wiki.chiu.micro.cache.adapter.out.redis;

import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import wiki.chiu.micro.cache.handler.CacheKeyLease;
import wiki.chiu.micro.cache.handler.CacheKeyRegistry;
import wiki.chiu.micro.cache.key.CacheDescriptor;

public final class RedisCacheKeyRegistry implements CacheKeyRegistry {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheKeyRegistry.class);
    private final RedissonClient redis;
    private final Set<CacheDescriptor> contracts = ConcurrentHashMap.newKeySet();
    private final String leaseScript = resource("cache-key-lease.lua");
    private final String pruneScript = resource("cache-key-prune.lua");
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(
        Thread.ofPlatform().daemon(true).name("cache-key-cleaner").factory());

    public RedisCacheKeyRegistry(RedissonClient redis) {
        this.redis = redis;
        cleaner.scheduleWithFixedDelay(this::clean, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public Set<String> registeredKeys(CacheDescriptor descriptor) {
        List<String> keys = redis.getScript(StringCodec.INSTANCE).eval(
            RScript.Mode.READ_WRITE, pruneScript, RScript.ReturnType.LIST,
            List.of(directory(descriptor)), "list");
        return Set.copyOf(keys);
    }

    @Override
    public boolean register(CacheDescriptor descriptor, String key) {
        return registerLease(descriptor, key, Duration.ofMinutes(30)).newlyRegistered();
    }

    @Override
    public CacheKeyLease registerLease(CacheDescriptor descriptor, String key, Duration ttl) {
        return lease(descriptor, key, ttl, "register");
    }

    @Override
    public CacheKeyLease renewLease(CacheDescriptor descriptor, String key, Duration ttl) {
        return lease(descriptor, key, ttl, "renew");
    }

    private CacheKeyLease lease(CacheDescriptor descriptor, String key, Duration ttl, String operation) {
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Registry TTL must be positive");
        }
        contracts.add(descriptor);
        List<Long> result = redis.getScript(StringCodec.INSTANCE).eval(
            RScript.Mode.READ_WRITE, leaseScript, RScript.ReturnType.LIST,
            List.of(directory(descriptor)), key, Long.toString(Math.max(1, ttl.toMillis())), operation);
        return result.isEmpty() ? null : new CacheKeyLease(result.get(0) == 1, result.get(1), result.get(2));
    }

    @Override
    public void unregisterFailedLoad(CacheDescriptor descriptor, String key) {
        redis.getScoredSortedSet(directory(descriptor), StringCodec.INSTANCE).remove(key);
    }

    private void clean() {
        for (CacheDescriptor descriptor : contracts) {
            try {
                redis.getScript(StringCodec.INSTANCE).eval(
                    RScript.Mode.READ_WRITE, pruneScript, RScript.ReturnType.LIST,
                    List.of(directory(descriptor)), "prune");
                if (!redis.getBucket(directory(descriptor)).isExists()) {
                    contracts.remove(descriptor);
                }
            } catch (RuntimeException failure) {
                log.warn("Cache registry cleanup failed for {}", descriptor, failure);
            }
        }
    }

    private static String directory(CacheDescriptor descriptor) {
        return "megalith:cache:key-leases:v2:" + descriptor.namespace() + ":v" + descriptor.version();
    }

    private static String resource(String name) {
        try {
            return new ClassPathResource("script/" + name).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    @PreDestroy
    public void close() {
        cleaner.shutdownNow();
    }
}
