package wiki.chiu.micro.exhibit.consumer.cache;

import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.exhibit.wrapper.BlogWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * @author mingchiuli
 * @create 2023-04-02 11:12 pm
 */
@Component
public class CacheKeyGenerator {

    private static final Logger log = LoggerFactory.getLogger(CacheKeyGenerator.class);

    private final CommonCacheKeyGenerator commonCacheKeyGenerator;

    public CacheKeyGenerator(CommonCacheKeyGenerator commonCacheKeyGenerator) {
        this.commonCacheKeyGenerator = commonCacheKeyGenerator;
    }

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public HashSet<String> generateHotBlogsKeys(Integer year, Long count, Long countYear) {
        HashSet<String> keys = new HashSet<>();
        long pageNo = count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1;
        long pageYearNo = countYear % blogPageSize == 0 ? countYear / blogPageSize : countYear / blogPageSize + 1;

        for (long i = 1; i <= pageNo; i++) {
            Method method;
            try {
                method = BlogWrapper.class.getMethod("findPage", Integer.class, Integer.class);
                String key = commonCacheKeyGenerator.generateKey(method, i, null);
                keys.add(key);
            } catch (NoSuchMethodException e) {
                log.error("some exception happen...", e);
            }
        }

        for (long i = 1; i <= pageYearNo; i++) {
            Method method;
            try {
                method = BlogWrapper.class.getMethod("findPage", Integer.class, Integer.class);
                String key = commonCacheKeyGenerator.generateKey(method, i, year);
                keys.add(key);
            } catch (NoSuchMethodException e) {
                log.error("some exception happen...", e);
            }
        }
        return keys;
    }

    public HashSet<String> generateBlogKey(long countAfter, long countYearAfter, Integer year) {
        HashSet<String> keys = new HashSet<>();
        long pageBeforeNo = countAfter / blogPageSize + 1;
        long pageYearBeforeNo = countYearAfter / blogPageSize + 1;

        for (long i = 1; i <= pageBeforeNo; i++) {
            Method method;
            try {
                method = BlogWrapper.class.getMethod("findPage", Integer.class, Integer.class);
                String key = commonCacheKeyGenerator.generateKey(method, i, null);
                keys.add(key);
            } catch (NoSuchMethodException e) {
                log.error("some exception happen...", e);
            }
        }

        for (long i = 1; i <= pageYearBeforeNo; i++) {
            Method method;
            try {
                method = BlogWrapper.class.getMethod("findPage", Integer.class, Integer.class);
                String key = commonCacheKeyGenerator.generateKey(method, i, year);
                keys.add(key);
            } catch (NoSuchMethodException e) {
                log.error("some exception happen...", e);
            }

        }
        return keys;
    }
}
