package wiki.chiu.micro.cache.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.utils.ClassUtils;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Aspect
@Order(2)
public class CacheAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheAspect.class);

    private final RedissonClient redissonClient;

    private final CommonCacheKeyGenerator commonCacheKeyGenerator;

    private final ObjectMapper objectMapper;

    private final RedissonClient redisson;

    private final com.github.benmanes.caffeine.cache.Cache<String, Object> localCache;

    public CacheAspect(RedissonClient redissonClient, ObjectMapper objectMapper, CommonCacheKeyGenerator commonCacheKeyGenerator, RedissonClient redisson, com.github.benmanes.caffeine.cache.Cache<String, Object> localCache) {
        this.redissonClient = redissonClient;
        this.commonCacheKeyGenerator = commonCacheKeyGenerator;
        this.redisson = redisson;
        this.localCache = localCache;
        this.objectMapper = objectMapper;
    }

    @Pointcut("@annotation(org.chiu.micro.cache.annotation.Cache)")
    public void pt() {
    }

    private static final String LOCK = "authLock:";

    @Around("pt()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        // 类名
        // 调用的方法名
        String methodName = signature.getName();
        Class<?> declaringType = signature.getDeclaringType();
        Object[] args = pjp.getArgs();
        Class<?>[] parameterTypes = ClassUtils.findClassArray(args);

        // 参数
        Method method = declaringType.getMethod(methodName, parameterTypes);
        Type genericReturnType = method.getGenericReturnType();

        JavaType javaType = objectMapper.getTypeFactory().constructType(genericReturnType);

        String cacheKey = commonCacheKeyGenerator.generateKey(method, args);

        Object cacheValue = localCache.getIfPresent(cacheKey);
        if (Objects.nonNull(cacheValue)) {
            return cacheValue;
        }

        Object localCacheObj = localCache.getIfPresent(cacheKey);

        if (Objects.nonNull(localCacheObj)) {
            return localCacheObj;
        }

        String remoteCacheStr;
        // 防止redis挂了以后db也访问不了
        try {
            remoteCacheStr = redissonClient.<String>getBucket(cacheKey).get();
        } catch (NestedRuntimeException e) {
            return pjp.proceed();
        }

        if (StringUtils.hasLength(remoteCacheStr)) {
            Object remoteCacheObj;
            try {
                remoteCacheObj = objectMapper.readValue(remoteCacheStr, javaType);
                localCache.put(cacheKey, remoteCacheObj);
                return remoteCacheObj;
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                return pjp.proceed();
            }
        }

        String lock = LOCK + cacheKey;
        // 已经线程安全
        RLock rLock = redisson.getLock(lock);

        try {
            boolean b = rLock.tryLock(5000, TimeUnit.MILLISECONDS);
            if (Boolean.FALSE.equals(b)) {
                return pjp.proceed();
            }
        } catch (InterruptedException e) {
            return pjp.proceed();
        }

        try {
            // 双重检查
            String r = redissonClient.<String>getBucket(cacheKey).get();

            if (StringUtils.hasLength(r)) {
                try {
                    return objectMapper.readValue(r, javaType);
                } catch (JsonProcessingException e) {
                    return pjp.proceed();
                }
            }
            // 执行目标方法
            Object proceed = pjp.proceed();

            Cache annotation = method.getAnnotation(Cache.class);
            try {
                redissonClient.getBucket(cacheKey).set(objectMapper.writeValueAsString(proceed), Duration.ofMinutes(annotation.expire()));
            } catch (JsonProcessingException e) {
                return proceed;
            }
            localCache.put(cacheKey, proceed);
            return proceed;
        } finally {
            rLock.unlock();
        }
    }
}