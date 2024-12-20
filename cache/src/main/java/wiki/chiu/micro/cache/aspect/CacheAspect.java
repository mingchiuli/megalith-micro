package wiki.chiu.micro.cache.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Order(2)
public class CacheAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheAspect.class);

    private static final String LOCK = "megalithRemoteLock:";
    private static final long LOCK_TIMEOUT = 5000;

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final CommonCacheKeyGenerator commonCacheKeyGenerator;
    private final com.github.benmanes.caffeine.cache.Cache<String, Object> localCache;
    private final com.github.benmanes.caffeine.cache.Cache<String, ReentrantLock> localLockMap;

    public CacheAspect(RedissonClient redissonClient, ObjectMapper objectMapper, CommonCacheKeyGenerator commonCacheKeyGenerator, com.github.benmanes.caffeine.cache.Cache<String, Object> localCache, com.github.benmanes.caffeine.cache.Cache<String, ReentrantLock> localLockMap) {
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
        this.commonCacheKeyGenerator = commonCacheKeyGenerator;
        this.localCache = localCache;
        this.localLockMap = localLockMap;
    }

    @Pointcut("@annotation(wiki.chiu.micro.cache.annotation.Cache)")
    public void pt() {
    }

    @Around("pt()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object[] args = pjp.getArgs();
        Type genericReturnType = method.getGenericReturnType();
        JavaType javaType = objectMapper.getTypeFactory().constructType(genericReturnType);
        String cacheKey = commonCacheKeyGenerator.generateKey(method, args);

        Object localCacheObj = getLocalCache(cacheKey);
        if (localCacheObj != null) {
            return localCacheObj;
        }

        ReentrantLock localLock = localLockMap.get(cacheKey, _ -> new ReentrantLock());
        if (!tryLock(localLock)) {
            return pjp.proceed();
        }

        try {
            localCacheObj = getLocalCache(cacheKey);
            if (localCacheObj != null) {
                return localCacheObj;
            }

            String remoteCacheStr = getRemoteCache(cacheKey);
            if (StringUtils.hasLength(remoteCacheStr)) {
                return parseRemoteCache(remoteCacheStr, javaType, cacheKey);
            }

            return handleCacheMiss(pjp, method, cacheKey);
        } finally {
            localLock.unlock();
        }
    }

    private Object getLocalCache(String cacheKey) {
        return localCache.getIfPresent(cacheKey);
    }

    private boolean tryLock(Lock lock) {
        try {
            return lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    private String getRemoteCache(String cacheKey) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        try {
            return bucket.get();
        } catch (NestedRuntimeException e) {
            return "";
        }
    }

    private Object parseRemoteCache(String remoteCacheStr, JavaType javaType, String cacheKey) throws JsonProcessingException {
        Object remoteCacheObj = objectMapper.readValue(remoteCacheStr, javaType);
        localCache.put(cacheKey, remoteCacheObj);
        return remoteCacheObj;
    }

    private Object handleCacheMiss(ProceedingJoinPoint pjp, Method method, String cacheKey) throws Throwable {
        RLock remoteLock = redissonClient.getLock(LOCK + cacheKey);
        if (!tryLock(remoteLock)) {
            return pjp.proceed();
        }

        try {
            String remoteCacheStr = getRemoteCache(cacheKey);
            if (StringUtils.hasLength(remoteCacheStr)) {
                return parseRemoteCache(remoteCacheStr, objectMapper.getTypeFactory().constructType(method.getGenericReturnType()), cacheKey);
            }

            Object proceed = pjp.proceed();
            Cache annotation = method.getAnnotation(Cache.class);
            cacheResult(cacheKey, proceed, annotation.expire());
            return proceed;
        } finally {
            remoteLock.unlock();
        }
    }

    private void cacheResult(String cacheKey, Object result, int expireMinutes) {
        try {
            String resultStr = objectMapper.writeValueAsString(result);
            redissonClient.getBucket(cacheKey).set(resultStr, Duration.ofMinutes(expireMinutes));
            localCache.put(cacheKey, result);
        } catch (JsonProcessingException e) {
            log.error("Error caching result", e);
        }
    }
}