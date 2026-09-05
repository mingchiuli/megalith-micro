package wiki.chiu.micro.cache.store;

import java.util.Set;

import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import wiki.chiu.micro.cache.handler.CacheKeyRegistry;
import wiki.chiu.micro.cache.key.CacheDescriptor;

public final class RedisCacheKeyRegistry implements CacheKeyRegistry {

    private final RedissonClient redis;

    public RedisCacheKeyRegistry(RedissonClient redis) {
        this.redis = redis;
    }

    @Override
    public Set<String> registeredKeys(CacheDescriptor descriptor) {
        return Set.copyOf(directory(descriptor).readAll());
    }

    public boolean register(CacheDescriptor descriptor, String key) {
        return directory(descriptor).add(key);
    }

    public void unregisterFailedLoad(CacheDescriptor descriptor, String key) {
        directory(descriptor).remove(key);
    }

    private RSet<String> directory(CacheDescriptor descriptor) {
        return redis.getSet(
            "megalith:cache:keys:" + descriptor.namespace() + ":v" + descriptor.version(),
            StringCodec.INSTANCE);
    }
}
