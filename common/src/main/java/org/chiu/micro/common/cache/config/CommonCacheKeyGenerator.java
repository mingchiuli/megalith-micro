package org.chiu.micro.common.cache.config;


import org.chiu.micro.common.cache.Cache;
import org.chiu.micro.common.cache.CacheAspect;
import org.chiu.micro.common.utils.JsonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2023-04-02 11:12 pm
 */
@Component
@ConditionalOnBean(CacheAspect.class)
public class CommonCacheKeyGenerator {

    private final JsonUtils jsonUtils;

    public CommonCacheKeyGenerator(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

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
                    params.append(jsonUtils.writeValueAsString(arg));
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
