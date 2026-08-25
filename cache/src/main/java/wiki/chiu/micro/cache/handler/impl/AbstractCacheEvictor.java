package wiki.chiu.micro.cache.handler.impl;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.CacheLockNames;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

abstract class AbstractCacheEvictor implements CacheEvictor {

  private final RedissonClient redissonClient;
  private final Cache<@NonNull String, LocalCacheEntry> localCache;
  private final CacheProperties properties;
  private final CacheMetrics metrics;

  AbstractCacheEvictor(
      RedissonClient redissonClient,
      Cache<@NonNull String, LocalCacheEntry> localCache,
      CacheProperties properties,
      CacheMetrics metrics) {
    this.redissonClient = redissonClient;
    this.localCache = localCache;
    this.properties = properties;
    this.metrics = metrics;
  }

  @Override
  public final void evict(Set<String> keys) {
    Set<String> immutableKeys = Set.copyOf(keys);
    if (immutableKeys.isEmpty()) {
      return;
    }
    immutableKeys.forEach(this::validateKey);

    try {
      List<String> sortedKeys = immutableKeys.stream().sorted().toList();
      List<RLock> locks = sortedKeys.stream().map(this::lock).toList();
      int acquired = acquireAll(sortedKeys, locks);
      try {
        redissonClient.getKeys().delete(sortedKeys.toArray(String[]::new));
        localCache.invalidateAll(immutableKeys);
        broadcast(CacheEvictionMessage.of(immutableKeys));
      } finally {
        unlockAll(locks, acquired);
      }
      metrics.eviction(transport(), "success");
    } catch (RuntimeException e) {
      metrics.eviction(transport(), "failure");
      throw e;
    }
  }

  protected abstract String transport();

  protected abstract void broadcast(CacheEvictionMessage message);

  private void validateKey(String key) {
    if (key.isBlank()) {
      throw new IllegalArgumentException("Cache eviction key must not be blank");
    }
  }

  private RLock lock(String key) {
    return redissonClient.getLock(CacheLockNames.remote(key));
  }

  private int acquireAll(List<String> keys, List<RLock> locks) {
    int acquired = 0;
    try {
      for (; acquired < locks.size(); acquired++) {
        if (!locks
            .get(acquired)
            .tryLock(
                properties.getSingleFlight().getWaitTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
          metrics.lockTimeout("evict");
          throw new IllegalStateException(
              "Timed out waiting to evict cache key " + keys.get(acquired));
        }
      }
      return acquired;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      metrics.failure("evict_lock_interrupted");
      unlockAll(locks, acquired);
      throw new IllegalStateException("Interrupted while acquiring cache eviction locks", e);
    } catch (RuntimeException e) {
      unlockAll(locks, acquired);
      throw e;
    }
  }

  private void unlockAll(List<RLock> locks, int acquired) {
    RuntimeException failure = null;
    for (int index = acquired - 1; index >= 0; index--) {
      try {
        locks.get(index).unlock();
      } catch (RuntimeException e) {
        metrics.failure("evict_unlock");
        if (failure == null) {
          failure = e;
        } else {
          failure.addSuppressed(e);
        }
      }
    }
    if (failure != null) {
      throw failure;
    }
  }
}
