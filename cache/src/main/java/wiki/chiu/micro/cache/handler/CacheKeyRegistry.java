package wiki.chiu.micro.cache.handler;

import java.util.Set;

import wiki.chiu.micro.cache.key.CacheDescriptor;

/** Provides the exact keys recorded by a cache contract with key tracking enabled. */
public interface CacheKeyRegistry {

    /**
     * Returns a snapshot including keys whose remote values may have expired while L1 remains live.
     *
     * @param descriptor the tracked cache contract
     * @return registered keys, retained across evictions
     */
    Set<String> registeredKeys(CacheDescriptor descriptor);
}
