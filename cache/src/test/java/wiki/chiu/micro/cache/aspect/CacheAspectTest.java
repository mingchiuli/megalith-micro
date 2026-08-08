package wiki.chiu.micro.cache.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.lang.reflect.Method;
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
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;

class CacheAspectTest {

  private final RedissonClient redisson = mock(RedissonClient.class);
  private final CommonCacheKeyGenerator keys = mock(CommonCacheKeyGenerator.class);
  private final com.github.benmanes.caffeine.cache.Cache<String, Object> local =
      Caffeine.newBuilder().build();
  private final com.github.benmanes.caffeine.cache.Cache<String, ReentrantLock> locks =
      Caffeine.newBuilder().build();
  private final CacheAspect aspect =
      new CacheAspect(redisson, JsonMapper.builder().build(), keys, local, locks);

  @AfterEach
  void clearInterruptedStatus() {
    Thread.interrupted();
  }

  @Test
  void returnsLocalValueWithoutCallingRemoteOrTarget() throws Throwable {
    ProceedingJoinPoint joinPoint = joinPoint();
    local.put("cache:key", "local");

    assertEquals("local", aspect.around(joinPoint));

    verify(redisson, never()).getBucket(anyString());
    verify(joinPoint, never()).proceed();
  }

  @Test
  void readsRemoteValueAndPopulatesLocalCache() throws Throwable {
    ProceedingJoinPoint joinPoint = joinPoint();
    @SuppressWarnings("unchecked")
    RBucket<String> bucket = mock(RBucket.class);
    when(redisson.<String>getBucket("cache:key")).thenReturn(bucket);
    when(bucket.get()).thenReturn("\"remote\"");

    assertEquals("remote", aspect.around(joinPoint));
    assertEquals("remote", local.getIfPresent("cache:key"));
    verify(joinPoint, never()).proceed();
  }

  @Test
  void writesTargetResultToRemoteAndLocalCaches() throws Throwable {
    ProceedingJoinPoint joinPoint = joinPoint();
    @SuppressWarnings("unchecked")
    RBucket<String> bucket = mock(RBucket.class);
    RLock remoteLock = mock(RLock.class);
    when(redisson.<String>getBucket("cache:key")).thenReturn(bucket);
    when(redisson.getLock("megalithRemoteLock:cache:key")).thenReturn(remoteLock);
    when(remoteLock.tryLock(5000, TimeUnit.MILLISECONDS)).thenReturn(true);
    when(joinPoint.proceed()).thenReturn("loaded");

    assertEquals("loaded", aspect.around(joinPoint));

    verify(bucket).set("\"loaded\"", Duration.ofMinutes(2));
    assertEquals("loaded", local.getIfPresent("cache:key"));
    verify(remoteLock).unlock();
  }

  @Test
  void interruptedLocalLockFallsBackAndRestoresInterruptFlag() throws Throwable {
    ProceedingJoinPoint joinPoint = joinPoint();
    when(joinPoint.proceed()).thenReturn("fallback");
    Thread.currentThread().interrupt();

    assertEquals("fallback", aspect.around(joinPoint));
    assertTrue(Thread.currentThread().isInterrupted());
  }

  private ProceedingJoinPoint joinPoint() throws NoSuchMethodException {
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    MethodSignature signature = mock(MethodSignature.class);
    Method method = Fixture.class.getDeclaredMethod("load", String.class);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getMethod()).thenReturn(method);
    when(joinPoint.getArgs()).thenReturn(new Object[] {"value"});
    when(keys.generateKey(eq(method), any())).thenReturn("cache:key");
    return joinPoint;
  }

  static final class Fixture {

    @Cache(prefix = "fixture", expire = 2)
    String load(String value) {
      return value;
    }
  }
}
