package wiki.chiu.micro.cache.handler;

import java.time.Duration;
import java.util.Set;

import wiki.chiu.micro.cache.key.CacheDescriptor;

/** Provides the exact keys recorded by a cache contract with key tracking enabled. */
public interface CacheKeyRegistry {

    /** Registers before loading; unsupported providers bypass tracked caching. */
    default CacheKeyLease registerLease(CacheDescriptor descriptor, String key, Duration ttl) {
        return null;
    }

    /** Extends an unexpired lease before cache creation; an expired load must not refill. */
    default CacheKeyLease renewLease(CacheDescriptor descriptor, String key, Duration ttl) {
        return null;
    }

    /**
     * Returns a snapshot including keys whose remote values may have expired while L1 remains live.
     *
     * @param descriptor the tracked cache contract
     * @return registered keys, retained across evictions until their leases expire
     */
    Set<String> registeredKeys(CacheDescriptor descriptor);

    /**
     * Records a key under a cache contract so it can be evicted exactly.
     *
     * @param descriptor the cache contract
     * @param key the generated key
     * @return true when the key was not registered before
     */
    boolean register(CacheDescriptor descriptor, String key);

    /**
     * Forgets a key whose read-through load failed.
     *
     * @param descriptor the cache contract
     * @param key the generated key
     */
    void unregisterFailedLoad(CacheDescriptor descriptor, String key);
}
