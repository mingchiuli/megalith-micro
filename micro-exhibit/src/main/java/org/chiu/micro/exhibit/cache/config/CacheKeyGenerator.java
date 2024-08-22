package org.chiu.micro.exhibit.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
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
@RequiredArgsConstructor
public class CacheKeyGenerator {

    private final ObjectMapper objectMapper;

    @Value("${blog.blog-page-size}")
    private int blogPageSize;

    private static final String FIND_PAGE = "findPage";

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

    @SneakyThrows
    public Set<String> generateHotBlogsKeys(Integer year, Long count, Long countYear) {
        Set<String> keys = new HashSet<>();
        long pageNo = count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1;
        long pageYearNo = countYear % blogPageSize == 0 ? countYear / blogPageSize : countYear / blogPageSize + 1;

        for (long i = 1; i <= pageNo; i++) {
            Method method = BlogWrapper.class.getMethod(FIND_PAGE, Integer.class, Integer.class);
            String key = generateKey(method, i, Integer.MIN_VALUE);
            keys.add(key);
        }

        for (long i = 1; i <= pageYearNo; i++) {
            Method method = BlogWrapper.class.getMethod(FIND_PAGE, Integer.class, Integer.class);
            String key = generateKey(method, i, year);
            keys.add(key);
        }
        return keys;
    }

    @SneakyThrows
    public Set<String> generateBlogKey(long countAfter, long countYearAfter, Integer year) {
        Set<String> keys = new HashSet<>();
        long pageBeforeNo = countAfter / blogPageSize + 1;
        long pageYearBeforeNo = countYearAfter / blogPageSize + 1;

        for (long i = 1; i <= pageBeforeNo; i++) {
            Method method = BlogWrapper.class.getMethod(FIND_PAGE, Integer.class, Integer.class);
            String key = generateKey(method, i, Integer.MIN_VALUE);
            keys.add(key);
        }

        for (long i = 1; i <= pageYearBeforeNo; i++) {
            Method method = BlogWrapper.class.getMethod(FIND_PAGE, Integer.class, Integer.class);
            String key = generateKey(method, i, year);
            keys.add(key);
        }
        return keys;
    }
}
