package org.chiu.micro.exhibit.cache.config;

import org.chiu.micro.common.utils.JsonUtils;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.chiu.micro.common.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author mingchiuli
 * @create 2023-04-02 11:12 pm
 */
@Component
public class CacheKeyGenerator {

    private static final Logger log = LoggerFactory.getLogger(CacheKeyGenerator.class);
    private final JsonUtils jsonUtils;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    private static final String FIND_PAGE = "findPage";

    public CacheKeyGenerator(JsonUtils jsonUtils) {
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

    public Set<String> generateHotBlogsKeys(Integer year, Long count, Long countYear) {
        Set<String> keys = new HashSet<>();
        long pageNo = count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1;
        long pageYearNo = countYear % blogPageSize == 0 ? countYear / blogPageSize : countYear / blogPageSize + 1;

        for (long i = 1; i <= pageNo; i++) {
            Method method;
            try {
                method = BlogWrapper.class.getMethod(FIND_PAGE, Integer.class, Integer.class);
                String key = generateKey(method, i, Integer.MIN_VALUE);
                keys.add(key);
            } catch (NoSuchMethodException e) {
                log.error("some exception happen...", e);
            }
        }

        for (long i = 1; i <= pageYearNo; i++) {
            Method method;
            try {
                method = BlogWrapper.class.getMethod(FIND_PAGE, Integer.class, Integer.class);
                String key = generateKey(method, i, year);
                keys.add(key);
            } catch (NoSuchMethodException e) {
                log.error("some exception happen...", e);
            }
        }
        return keys;
    }

    public Set<String> generateBlogKey(long countAfter, long countYearAfter, Integer year) {
        Set<String> keys = new HashSet<>();
        long pageBeforeNo = countAfter / blogPageSize + 1;
        long pageYearBeforeNo = countYearAfter / blogPageSize + 1;

        for (long i = 1; i <= pageBeforeNo; i++) {
            Method method;
            try {
                method = BlogWrapper.class.getMethod(FIND_PAGE, Integer.class, Integer.class);
                String key = generateKey(method, i, Integer.MIN_VALUE);
                keys.add(key);
            } catch (NoSuchMethodException e) {
                log.error("some exception happen...", e);
            }
        }

        for (long i = 1; i <= pageYearBeforeNo; i++) {
            Method method;
            try {
                method = BlogWrapper.class.getMethod(FIND_PAGE, Integer.class, Integer.class);
                String key = generateKey(method, i, year);
                keys.add(key);
            } catch (NoSuchMethodException e) {
                log.error("some exception happen...", e);
            }

        }
        return keys;
    }
}
