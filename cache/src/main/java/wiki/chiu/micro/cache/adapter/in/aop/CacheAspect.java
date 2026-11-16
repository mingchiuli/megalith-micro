package wiki.chiu.micro.cache.adapter.in.aop;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.application.CacheLockNames;
import wiki.chiu.micro.cache.application.CacheMetrics;
import wiki.chiu.micro.cache.application.model.LocalCacheEntry;
import wiki.chiu.micro.cache.handler.CacheKeyLease;
import wiki.chiu.micro.cache.handler.CacheKeyRegistry;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;

@Aspect
@Order(2)
public class CacheAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheAspect.class);
    private static final String TRACKED_WRITE = trackedWriteScript();

    private static String trackedWriteScript() {
        try {
            return new ClassPathResource("script/cache-tracked-write.lua")
                .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private final RedissonClient redissonClient;
    private final JsonMapper jsonMapper;
    private final CacheKeyFactory cacheKeyFactory;
    private final com.github.benmanes.caffeine.cache.Cache<@NonNull String, LocalCacheEntry>
        localCache;
    private final com.github.benmanes.caffeine.cache.Cache<@NonNull String, ReentrantLock>
        localLockMap;
    private final Duration singleFlightWaitTimeout;
    private final double localTtlJitter;
    private final CacheMetrics metrics;
    private final CacheKeyRegistry keyRegistry;

    public CacheAspect(
        RedissonClient redissonClient,
        JsonMapper jsonMapper,
        CacheKeyFactory cacheKeyFactory,
        com.github.benmanes.caffeine.cache.Cache<@NonNull String, LocalCacheEntry> localCache,
        com.github.benmanes.caffeine.cache.Cache<@NonNull String, ReentrantLock> localLockMap,
        Duration singleFlightWaitTimeout,
        double localTtlJitter,
        CacheMetrics metrics,
        CacheKeyRegistry keyRegistry) {
        this.redissonClient = redissonClient;
        this.jsonMapper = jsonMapper;
        this.cacheKeyFactory = cacheKeyFactory;
        this.localCache = localCache;
        this.localLockMap = localLockMap;
        this.singleFlightWaitTimeout = singleFlightWaitTimeout;
        this.localTtlJitter = localTtlJitter;
        this.metrics = metrics;
        this.keyRegistry = keyRegistry;
    }

    @Pointcut("@annotation(wiki.chiu.micro.cache.annotation.Cache)")
    public void pt() {
    }

    @Around("pt()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = resolveMethod(pjp);
        Cache annotation =
            AnnotatedElementUtils.findMergedAnnotation(method, Cache.class);
        if (annotation == null) {
            throw new IllegalStateException("Cache annotation not found on " + method);
        }

        Duration ttl = cacheTtl(annotation, method);
        CacheDescriptor descriptor = new CacheDescriptor(annotation.namespace(), annotation.version());
        String cacheKey =
            cacheKeyFactory.generate(
                descriptor, pjp.getArgs());

        Object localValue = localValue(cacheKey);
        if (localValue != null) {
            metrics.request("l1_hit");
            return localValue;
        }

        ReentrantLock localLock = localLockMap.get(cacheKey, _ -> new ReentrantLock());
        if (!tryLocalLock(localLock)) {
            return proceedUncached(pjp);
        }

        try {
            localValue = localValue(cacheKey);
            if (localValue != null) {
                metrics.request("l1_hit");
                return localValue;
            }

            if (annotation.trackKeys()) {
                return handleMiss(pjp, method, cacheKey, ttl, descriptor, true);
            }

            RemoteRead remoteRead = readRemote(cacheKey);
            if (!remoteRead.available()) {
                return proceedAndCacheLocally(pjp, cacheKey, ttl);
            }
            if (remoteRead.value() != null) {
                Object remoteValue = parseRemote(remoteRead.value(), method, cacheKey, ttl);
                if (remoteValue != null) {
                    metrics.request("l2_hit");
                    return remoteValue;
                }
            }

            return handleMiss(pjp, method, cacheKey, ttl, descriptor, false);
        } finally {
            localLock.unlock();
        }
    }

    private Method resolveMethod(ProceedingJoinPoint pjp) {
        Method signatureMethod = ((MethodSignature) pjp.getSignature()).getMethod();
        Object target = pjp.getTarget();
        Method specificMethod =
            target == null
                ? signatureMethod
                : AopUtils.getMostSpecificMethod(signatureMethod, target.getClass());
        return BridgeMethodResolver.findBridgedMethod(specificMethod);
    }

    private Duration cacheTtl(Cache annotation, Method method) {
        if (annotation.ttl() <= 0) {
            throw new IllegalStateException("Cache TTL must be positive on " + method);
        }
        long ttlNanos = annotation.timeUnit().toNanos(annotation.ttl());
        if (ttlNanos <= 0) {
            throw new IllegalStateException("Cache TTL is too small on " + method);
        }
        return Duration.ofNanos(ttlNanos);
    }

    private Object localValue(String cacheKey) {
        LocalCacheEntry entry = localCache.getIfPresent(cacheKey);
        return entry == null ? null : entry.value();
    }

    private boolean tryLocalLock(Lock lock) {
        try {
            boolean acquired =
                lock.tryLock(singleFlightWaitTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                metrics.lockTimeout("local");
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            metrics.failure("local_lock_interrupted");
            return false;
        }
    }

    private RemoteLockResult tryRemoteLock(RLock lock) {
        try {
            boolean acquired =
                lock.tryLock(singleFlightWaitTimeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                metrics.lockTimeout("remote");
                return RemoteLockResult.TIMEOUT;
            }
            return RemoteLockResult.ACQUIRED;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            metrics.failure("remote_lock_interrupted");
            return RemoteLockResult.INTERRUPTED;
        } catch (RedisException e) {
            metrics.failure("remote_lock");
            log.warn("Remote cache lock failed for {}", lock.getName(), e);
            return RemoteLockResult.UNAVAILABLE;
        }
    }

    private RemoteRead readRemote(String cacheKey) {
        try {
            return new RemoteRead(true, remoteBucket(cacheKey).get());
        } catch (RedisException e) {
            metrics.failure("l2_read");
            log.warn("Remote cache read failed for {}", cacheKey, e);
            return new RemoteRead(false, null);
        }
    }

    private Object parseRemote(
        String remoteValue, Method method, String cacheKey, Duration ttl) {
        Type genericReturnType = method.getGenericReturnType();
        JavaType javaType = jsonMapper.getTypeFactory().constructType(genericReturnType);
        try {
            Object value = jsonMapper.readValue(remoteValue, javaType);
            if (value == null) {
                deleteCorruptRemote(cacheKey);
                return null;
            }
            if (!ttl.isZero() && !ttl.isNegative()) {
                putLocal(cacheKey, value, ttl);
            }
            return value;
        } catch (JacksonException e) {
            metrics.failure("deserialize");
            log.warn("Remote cache value is invalid for {}", cacheKey, e);
            deleteCorruptRemote(cacheKey);
            return null;
        }
    }

    private void deleteCorruptRemote(String cacheKey) {
        try {
            remoteBucket(cacheKey).delete();
        } catch (RedisException e) {
            metrics.failure("l2_delete");
            log.warn("Invalid remote cache value could not be deleted for {}", cacheKey, e);
        }
    }

    private Object handleMiss(
        ProceedingJoinPoint pjp, Method method, String cacheKey, Duration ttl,
        CacheDescriptor descriptor, boolean tracked) throws Throwable {
        RLock remoteLock = redissonClient.getLock(CacheLockNames.remote(cacheKey));
        RemoteLockResult lockResult = tryRemoteLock(remoteLock);
        if (lockResult == RemoteLockResult.TIMEOUT || lockResult == RemoteLockResult.INTERRUPTED) {
            return proceedUncached(pjp);
        }
        if (lockResult == RemoteLockResult.UNAVAILABLE) {
            return tracked ? proceedUncached(pjp) : proceedAndCacheLocally(pjp, cacheKey, ttl);
        }

        try {
            boolean newlyRegistered = false;
            if (tracked) {
                try {
                    CacheKeyLease registration = keyRegistry.registerLease(descriptor, cacheKey, ttl);
                    if (registration == null) {
                        return proceedUncached(pjp);
                    }
                    newlyRegistered = registration.newlyRegistered();
                } catch (RedisException failure) {
                    metrics.failure("key_registration");
                    log.warn("Cache key registration failed for {}", cacheKey, failure);
                    return proceedUncached(pjp);
                }
            }
            boolean loaded = false;
            try {
                RemoteRead remoteRead = readRemote(cacheKey);
                if (!remoteRead.available()) {
                    return tracked ? proceedUncached(pjp) : proceedAndCacheLocally(pjp, cacheKey, ttl);
                }
                if (remoteRead.value() != null) {
                    Object remoteValue = parseRemote(remoteRead.value(), method, cacheKey,
                        tracked ? Duration.ZERO : ttl);
                    if (remoteValue != null) {
                        if (tracked) {
                            long started = System.nanoTime();
                            try {
                                CacheKeyLease lease = keyRegistry.renewLease(descriptor, cacheKey, ttl);
                                Duration localTtl = leasedTtl(lease, started, ttl);
                                if (!localTtl.isZero()) {
                                    putLocal(cacheKey, remoteValue, localTtl);
                                }
                            } catch (RedisException failure) {
                                metrics.failure("key_renewal");
                                log.warn("Cache lease renewal failed for {}", cacheKey, failure);
                            }
                        }
                        loaded = true;
                        metrics.request("l2_hit");
                        return remoteValue;
                    }
                }
                Object result = proceed(pjp);
                if (result == null) {
                    return null;
                }
                loaded = true;
                if (tracked) {
                    writeTrackedCaches(descriptor, cacheKey, result, ttl);
                } else {
                    writeCaches(cacheKey, result, ttl);
                }
                return result;
            } finally {
                if (newlyRegistered && !loaded) {
                    try {
                        keyRegistry.unregisterFailedLoad(descriptor, cacheKey);
                    } catch (RedisException failure) {
                        metrics.failure("key_registration_cleanup");
                        log.warn("Could not remove failed cache registration {}", cacheKey, failure);
                    }
                }
            }
        } finally {
            unlockRemote(remoteLock);
        }
    }

    private Object proceedAndCacheLocally(
        ProceedingJoinPoint pjp, String cacheKey, Duration ttl) throws Throwable {
        Object result = proceed(pjp);
        if (result != null) {
            putLocal(cacheKey, result, ttl);
        }
        return result;
    }

    private Object proceedUncached(ProceedingJoinPoint pjp) throws Throwable {
        return proceed(pjp);
    }

    private Object proceed(ProceedingJoinPoint pjp) throws Throwable {
        metrics.request("source");
        return pjp.proceed();
    }

    private void writeTrackedCaches(CacheDescriptor descriptor, String key, Object result, Duration ttl) {
        long started = System.nanoTime();
        try {
            CacheKeyLease lease = keyRegistry.renewLease(descriptor, key, ttl);
            if (lease == null) {
                return;
            }
            String serialized = jsonMapper.writeValueAsString(result);
            Boolean written = redissonClient.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE, TRACKED_WRITE, RScript.ReturnType.BOOLEAN,
                List.of(key), serialized, Long.toString(Math.max(1, ttl.toMillis())),
                Long.toString(lease.expiresAtEpochMillis()));
            Duration localTtl = leasedTtl(lease, started, ttl);
            if (Boolean.TRUE.equals(written) && !localTtl.isZero()) {
                putLocal(key, result, localTtl);
            }
        } catch (JacksonException | RedisException failure) {
            metrics.failure("tracked_write");
            log.warn("Tracked cache write failed for {}", key, failure);
        }
    }

    private static Duration leasedTtl(CacheKeyLease lease, long started, Duration ttl) {
        if (lease == null) {
            return Duration.ZERO;
        }
        long remaining = TimeUnit.MILLISECONDS.toNanos(lease.remainingMillis()) - (System.nanoTime() - started);
        return Duration.ofNanos(Math.max(0, Math.min(ttl.toNanos(), remaining)));
    }

    private void writeCaches(String cacheKey, Object result, Duration ttl) {
        try {
            String resultJson = jsonMapper.writeValueAsString(result);
            remoteBucket(cacheKey).set(resultJson, ttl);
        } catch (JacksonException e) {
            metrics.failure("serialize");
            log.warn("Cache result could not be serialized for {}", cacheKey, e);
        } catch (RedisException e) {
            metrics.failure("l2_write");
            log.warn("Remote cache write failed for {}", cacheKey, e);
        }
        putLocal(cacheKey, result, ttl);
    }

    private void putLocal(String cacheKey, Object result, Duration ttl) {
        double jitter = localTtlJitter;
        double factor = jitter == 0 ? 1 : ThreadLocalRandom.current().nextDouble(1 - jitter, 1);
        long localTtlNanos = Math.max(1, (long) (ttl.toNanos() * factor));
        localCache.put(cacheKey, new LocalCacheEntry(result, Duration.ofNanos(localTtlNanos)));
    }

    private RBucket<String> remoteBucket(String cacheKey) {
        return redissonClient.getBucket(cacheKey, StringCodec.INSTANCE);
    }

    private void unlockRemote(RLock lock) {
        try {
            lock.unlock();
        } catch (RedisException e) {
            metrics.failure("remote_unlock");
            log.warn("Remote cache lock release failed for {}", lock.getName(), e);
        }
    }

    private record RemoteRead(boolean available, String value) {
    }

    private enum RemoteLockResult {
        ACQUIRED,
        TIMEOUT,
        INTERRUPTED,
        UNAVAILABLE
    }
}
