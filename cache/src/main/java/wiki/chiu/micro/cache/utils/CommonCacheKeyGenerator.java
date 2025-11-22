package wiki.chiu.micro.cache.utils;


import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.annotation.Cache;

import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author mingchiuli
 * @since 2023-04-02 11:12 pm
 */
public record CommonCacheKeyGenerator(JsonMapper jsonMapper) {

    private static final String NULL_PARAM = ":null";
    private static final String PARAM_SEPARATOR = ":";

    public String generateKey(Method method, Object... args) {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String params = buildParams(args);
        String prefix = getPrefix(method);

        return StringUtils.hasLength(prefix) ?
                prefix + PARAM_SEPARATOR + className + PARAM_SEPARATOR + methodName + params :
                className + PARAM_SEPARATOR + methodName + params;
    }

    private String buildParams(Object... args) {
        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            params.append(PARAM_SEPARATOR);
            if (Objects.nonNull(arg)) {
                params.append(arg instanceof String ? arg : jsonMapper.writeValueAsString(arg));
            } else {
                params.append(NULL_PARAM);
            }
        }
        return params.toString();
    }


    private String getPrefix(Method method) {
        Cache annotation = method.getAnnotation(Cache.class);
        return Objects.nonNull(annotation) ? annotation.prefix() : null;
    }
}
