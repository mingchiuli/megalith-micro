package org.chiu.micro.auth.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.chiu.micro.auth.cache.Cache;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2023-04-02 11:12 pm
 */
@Component
@RequiredArgsConstructor
public class CacheKeyGenerator {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public String generateKey(Method method, Object... args) {

        Class<?> declaringType = method.getDeclaringClass();
        String methodName = method.getName();

        var params = new StringBuilder();
        for (Object arg : args) {
            if (Objects.nonNull(arg)) {
                params.append("::");
                if (arg instanceof String) {
                    params.append(arg);
                } else {
                    params.append(objectMapper.writeValueAsString(arg));
                }
            }
        }

        String className = declaringType.getSimpleName();
        var annotation = method.getAnnotation(Cache.class);
        String prefix = null;
        if (Objects.nonNull(annotation)) {
            prefix = annotation.prefix().getInfo();
        }

        return StringUtils.hasLength(prefix) ?
                prefix + "::" + className + "::" + methodName + params :
                className + "::" + methodName + params;
    }
}
