package wiki.chiu.micro.cache.handler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

class CacheEvictorTest {

    private final RedissonClient redisson = mock(RedissonClient.class);
    private final RKeys remoteKeys = mock(RKeys.class);
    private final Cache<String, LocalCacheEntry> local = Caffeine.newBuilder().build();
    private final CacheProperties properties = new CacheProperties();

    @Test
    void locksSortedKeysUntilDeleteLocalInvalidationAndBroadcastComplete() throws Exception {
        RLock first = lock("a", true);
        RLock second = lock("b", true);
        when(redisson.getKeys()).thenReturn(remoteKeys);
        local.put("a", entry());
        local.put("b", entry());
        RecordingCacheEvictor evictor = evictor(false);

        evictor.evict(Set.of("b", "a"));

        assertThat(local.asMap()).isEmpty();
        assertThat(evictor.message.keys()).containsExactlyInAnyOrder("a", "b");
        verify(remoteKeys).delete(aryEq(new String[]{"a", "b"}));
        InOrder order = inOrder(redisson, first, second, remoteKeys);
        order.verify(redisson).getLock("megalithRemoteLock:a");
        order.verify(redisson).getLock("megalithRemoteLock:b");
        order.verify(first).tryLock(5000, TimeUnit.MILLISECONDS);
        order.verify(second).tryLock(5000, TimeUnit.MILLISECONDS);
        order.verify(remoteKeys).delete(aryEq(new String[]{"a", "b"}));
        order.verify(second).unlock();
        order.verify(first).unlock();
    }

    @Test
    void lockTimeoutFailsWithoutDeletingOrInvalidating() throws Exception {
        RLock first = lock("a", true);
        lock("b", false);
        when(redisson.getKeys()).thenReturn(remoteKeys);
        local.put("a", entry());
        RecordingCacheEvictor evictor = evictor(false);

        assertThatThrownBy(() -> evictor.evict(Set.of("a", "b")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Timed out waiting to evict cache key b");

        assertThat(local.getIfPresent("a")).isNotNull();
        verify(remoteKeys, never()).delete(org.mockito.ArgumentMatchers.<String[]>any());
        verify(first).unlock();
        assertThat(evictor.message).isNull();
    }

    @Test
    void broadcastFailureIsVisibleAfterRemoteAndLocalEviction() throws Exception {
        RLock lock = lock("a", true);
        when(redisson.getKeys()).thenReturn(remoteKeys);
        local.put("a", entry());
        RecordingCacheEvictor evictor = evictor(true);

        assertThatThrownBy(() -> evictor.evict(Set.of("a")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("broadcast failed");

        assertThat(local.getIfPresent("a")).isNull();
        verify(remoteKeys).delete(aryEq(new String[]{"a"}));
        verify(lock).unlock();
    }

    private RecordingCacheEvictor evictor(boolean failBroadcast) {
        return new RecordingCacheEvictor(
            redisson, local, properties, new CacheMetrics(null), failBroadcast);
    }

    private RLock lock(String key, boolean acquired) throws Exception {
        RLock lock = mock(RLock.class);
        when(redisson.getLock("megalithRemoteLock:" + key)).thenReturn(lock);
        when(lock.tryLock(5000, TimeUnit.MILLISECONDS)).thenReturn(acquired);
        return lock;
    }

    private LocalCacheEntry entry() {
        return new LocalCacheEntry("value", Duration.ofMinutes(1));
    }

    private static final class RecordingCacheEvictor extends AbstractCacheEvictor {

        private final boolean failBroadcast;
        private CacheEvictionMessage message;

        private RecordingCacheEvictor(
            RedissonClient redissonClient,
            Cache<String, LocalCacheEntry> localCache,
            CacheProperties properties,
            CacheMetrics metrics,
            boolean failBroadcast) {
            super(redissonClient, localCache, properties, metrics);
            this.failBroadcast = failBroadcast;
        }

        @Override
        protected String transport() {
            return "test";
        }

        @Override
        protected void broadcast(CacheEvictionMessage message) {
            this.message = message;
            if (failBroadcast) {
                throw new IllegalStateException("broadcast failed");
            }
        }
    }
}
