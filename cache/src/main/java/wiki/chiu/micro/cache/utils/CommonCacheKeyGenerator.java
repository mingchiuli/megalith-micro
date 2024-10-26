package wiki.chiu.micro.cache.utils;


import wiki.chiu.micro.cache.annotation.Cache;

import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2023-04-02 11:12 pm
 */
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
            prefix = annotation.prefix();
        }

        return StringUtils.hasLength(prefix) ?
                prefix + "::" + className + "::" + methodName + params :
                className + "::" + methodName + params;
    }
}
