package org.chiu.micro.exhibit.cache.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.chiu.micro.exhibit.utils.ClassUtils;
import org.redisson.api.*;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 统一缓存处理
 *
 * @author mingchiuli
 *         order: 多个切面执行顺序，越小越先执行
 * @create 2021-12-01 7:48 AM
 */
@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class CacheAspect {

    private final StringRedisTemplate redisTemplate;

    private final CacheKeyGenerator cacheKeyGenerator;

    private final ObjectMapper objectMapper;

    private final RedissonClient redisson;

    private final com.github.benmanes.caffeine.cache.Cache<String, Object> localCache;

    @Pointcut("@annotation(org.chiu.micro.exhibit.cache.config.Cache)")
    public void pt() {
    }

    private static final String LOCK = "blogLock:";

    @SneakyThrows
    @Around("pt()")
    public Object around(ProceedingJoinPoint pjp) {
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

        String cacheKey = cacheKeyGenerator.generateKey(method, args);

        Object cacheValue = localCache.getIfPresent(cacheKey);
        if (Objects.nonNull(cacheValue)) {
            return cacheValue;
        }

        String remoteCacheStr;
        // 防止redis挂了以后db也访问不了
        try {
            remoteCacheStr = redisTemplate.opsForValue().get(cacheKey);
        } catch (NestedRuntimeException e) {
            return pjp.proceed();
        }

        if (StringUtils.hasLength(remoteCacheStr)) {
            Object remoteCacheObj = objectMapper.readValue(remoteCacheStr, javaType);
            localCache.put(cacheKey, remoteCacheObj);
            return remoteCacheObj;
        }

        String lock = LOCK + cacheKey;
        // 已经线程安全
        RLock rLock = redisson.getLock(lock);

        if (Boolean.FALSE.equals(rLock.tryLock(5000, TimeUnit.MILLISECONDS))) {
            return pjp.proceed();
        }

        try {
            // 双重检查
            String r = redisTemplate.opsForValue().get(cacheKey);

            if (StringUtils.hasLength(r)) {
                return objectMapper.readValue(r, javaType);
            }
            // 执行目标方法
            Object proceed = pjp.proceed();

            Cache annotation = method.getAnnotation(Cache.class);
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(proceed), annotation.expire(), TimeUnit.MINUTES);
            localCache.put(cacheKey, proceed);
            return proceed;
        } finally {
            rLock.unlock();
        }
    }

}
