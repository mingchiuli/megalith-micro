package wiki.chiu.micro.cache.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.client.codec.StringCodec;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

class CacheAspectTest {

    private static final String KEY = "fixture:v1:digest";

    private final RedissonClient redisson = mock(RedissonClient.class);
    private final CacheKeyFactory keys = (_, _) -> KEY;
    private final com.github.benmanes.caffeine.cache.Cache<String, LocalCacheEntry> local =
        Caffeine.newBuilder().build();
    private final com.github.benmanes.caffeine.cache.Cache<String, ReentrantLock> locks =
        Caffeine.newBuilder().build();
    private final CacheProperties properties = properties();
    private final CacheAspect aspect =
        new CacheAspect(
            redisson,
            JsonMapper.builder().build(),
            keys,
            local,
            locks,
            properties,
            new CacheMetrics(null));

    @AfterEach
    void clearInterruptedStatus() {
        Thread.interrupted();
    }

    @Test
    void returnsLocalValueWithoutCallingRemoteOrTarget() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        local.put(KEY, new LocalCacheEntry("local", Duration.ofMinutes(1)));

        assertThat(aspect.around(joinPoint)).isEqualTo("local");

        verify(redisson, never()).getBucket(anyString(), org.mockito.ArgumentMatchers.any());
        verify(joinPoint, never()).proceed();
    }

    @Test
    void readsRemoteValueAndPopulatesLocalCache() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        RBucket<String> bucket = bucket();
        when(bucket.get()).thenReturn("\"remote\"");

        assertThat(aspect.around(joinPoint)).isEqualTo("remote");
        assertThat(local.getIfPresent(KEY).value()).isEqualTo("remote");
        verify(joinPoint, never()).proceed();
    }

    @Test
    void writesTargetResultToRemoteAndLocalCaches() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        RBucket<String> bucket = bucket();
        RLock remoteLock = remoteLock(true);
        when(joinPoint.proceed()).thenReturn("loaded");

        assertThat(aspect.around(joinPoint)).isEqualTo("loaded");

        verify(bucket).set("\"loaded\"", Duration.ofMinutes(2));
        assertThat(local.getIfPresent(KEY).value()).isEqualTo("loaded");
        verify(remoteLock).unlock();
    }

    @Test
    void skipsBothCachesForNullResults() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        RBucket<String> bucket = bucket();
        remoteLock(true);
        when(joinPoint.proceed()).thenReturn(null);

        assertThat(aspect.around(joinPoint)).isNull();

        assertThat(local.getIfPresent(KEY)).isNull();
        verify(bucket, never()).set(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void failsOpenAndOnlyPopulatesLocalCacheWhenRedisReadFails() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        RBucket<String> bucket = bucket();
        when(bucket.get()).thenThrow(new RedisException("offline"));
        when(joinPoint.proceed()).thenReturn("source");

        assertThat(aspect.around(joinPoint)).isEqualTo("source");

        assertThat(local.getIfPresent(KEY).value()).isEqualTo("source");
        verify(redisson, never()).getLock(anyString());
    }

    @Test
    void remoteLockTimeoutRunsSourceWithoutCaching() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        bucket();
        remoteLock(false);
        when(joinPoint.proceed()).thenReturn("source");

        assertThat(aspect.around(joinPoint)).isEqualTo("source");

        assertThat(local.getIfPresent(KEY)).isNull();
    }

    @Test
    void interruptedLocalLockFallsBackAndRestoresInterruptFlag() throws Throwable {
        ProceedingJoinPoint joinPoint = joinPoint();
        when(joinPoint.proceed()).thenReturn("fallback");
        Thread.currentThread().interrupt();

        assertThat(aspect.around(joinPoint)).isEqualTo("fallback");
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    @SuppressWarnings("unchecked")
    private RBucket<String> bucket() {
        RBucket<String> bucket = mock(RBucket.class);
        when(redisson.<String>getBucket(KEY, StringCodec.INSTANCE)).thenReturn(bucket);
        return bucket;
    }

    private RLock remoteLock(boolean acquired) throws InterruptedException {
        RLock remoteLock = mock(RLock.class);
        when(redisson.getLock("megalithRemoteLock:" + KEY)).thenReturn(remoteLock);
        when(remoteLock.tryLock(5000, TimeUnit.MILLISECONDS)).thenReturn(acquired);
        return remoteLock;
    }

    private ProceedingJoinPoint joinPoint() throws NoSuchMethodException {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(Fixture.class.getDeclaredMethod("load", String.class));
        when(joinPoint.getArgs()).thenReturn(new Object[]{"value"});
        return joinPoint;
    }

    private CacheProperties properties() {
        CacheProperties result = new CacheProperties();
        result.getLocal().setTtlJitter(0);
        return result;
    }

    static final class Fixture {

        @Cache(namespace = "fixture", ttl = 2)
        String load(String value) {
            return value;
        }
    }
}
