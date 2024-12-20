package wiki.chiu.micro.cache.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import wiki.chiu.micro.cache.annotation.Cache;

import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author mingchiuli
 * @since 2023-04-02 11:12 pm
 */
public class CommonCacheKeyGenerator {

    private static final String NULL_PARAM = ":null";
    private static final String PARAM_SEPARATOR = ":";
    private static final String EMPTY_STRING = "";

    private final ObjectMapper objectMapper;

    public CommonCacheKeyGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
                params.append(arg instanceof String ? arg : convertToString(arg));
            } else {
                params.append(NULL_PARAM);
            }
        }
        return params.toString();
    }

    private String convertToString(Object arg) {
        try {
            return objectMapper.writeValueAsString(arg);
        } catch (JsonProcessingException e) {
            return EMPTY_STRING;
        }
    }

    private String getPrefix(Method method) {
        Cache annotation = method.getAnnotation(Cache.class);
        return Objects.nonNull(annotation) ? annotation.prefix() : null;
    }
}
