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
import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Order(2)
public class CacheAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheAspect.class);

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

    private static final String LOCK = "megalithRemoteLock:";

    @Around("pt()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        // 类名
        // 调用的方法名
        Object[] args = pjp.getArgs();

        // 参数
        Method method = signature.getMethod();
        Type genericReturnType = method.getGenericReturnType();

        JavaType javaType = objectMapper.getTypeFactory().constructType(genericReturnType);

        String cacheKey = commonCacheKeyGenerator.generateKey(method, args);

        Object localCacheObj = localCache.getIfPresent(cacheKey);

        if (localCacheObj != null) {
            return localCacheObj;
        }

        ReentrantLock localLock = localLockMap.get(cacheKey, _ -> new ReentrantLock());

        try {
            boolean b = localLock.tryLock(5000, TimeUnit.MILLISECONDS);
            if (Boolean.FALSE.equals(b)) {
                return pjp.proceed();
            }
        } catch (InterruptedException e) {
            return pjp.proceed();
        }

        try {
            localCacheObj = localCache.getIfPresent(cacheKey);
            if (localCacheObj != null) {
                return localCacheObj;
            }

            String remoteCacheStr;
            RBucket<String> bucket = redissonClient.getBucket(cacheKey);
            // 防止redis挂了以后db也访问不了
            try {
                remoteCacheStr = bucket.get();
            } catch (NestedRuntimeException e) {
                return pjp.proceed();
            }

            if (StringUtils.hasLength(remoteCacheStr)) {
                try {
                    Object remoteCacheObj = objectMapper.readValue(remoteCacheStr, javaType);
                    localCache.put(cacheKey, remoteCacheObj);
                    return remoteCacheObj;
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                    return pjp.proceed();
                }
            }

            String lock = LOCK + cacheKey;
            // 已经线程安全
            RLock remoteLock = redissonClient.getLock(lock);

            try {
                boolean b = remoteLock.tryLock(5000, TimeUnit.MILLISECONDS);
                if (Boolean.FALSE.equals(b)) {
                    return pjp.proceed();
                }
            } catch (InterruptedException e) {
                return pjp.proceed();
            }

            try {
                // 双重检查
                String r = bucket.get();

                if (StringUtils.hasLength(r)) {
                    try {
                        Object value = objectMapper.readValue(r, javaType);
                        localCache.put(cacheKey, value);
                        return value;
                    } catch (JsonProcessingException e) {
                        return pjp.proceed();
                    }
                }
                // 执行目标方法
                Object proceed = pjp.proceed();

                Cache annotation = method.getAnnotation(Cache.class);
                try {
                    bucket.set(objectMapper.writeValueAsString(proceed), Duration.ofMinutes(annotation.expire()));
                } catch (JsonProcessingException e) {
                    return proceed;
                }
                localCache.put(cacheKey, proceed);
                return proceed;
            } finally {
                remoteLock.unlock();
            }
        } finally {
            localLock.unlock();
        }
    }
}