package wiki.chiu.micro.exhibit.adapter.in.messaging.cache;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.exhibit.cache.BlogCacheDescriptors;

@Component
public class CacheKeyGenerator {

    private final CacheKeyFactory cacheKeyFactory;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public CacheKeyGenerator(CacheKeyFactory cacheKeyFactory) {
        this.cacheKeyFactory = cacheKeyFactory;
    }

    public HashSet<String> generateHotBlogsKeys(Long count) {
        long pages = count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1;
        return generatePageKeys(Math.toIntExact(pages));
    }

    public HashSet<String> generateBlogKey(long countAfter) {
        long pages = countAfter / blogPageSize + 1;
        return generatePageKeys(Math.toIntExact(pages));
    }

    private HashSet<String> generatePageKeys(int pageCount) {
        HashSet<String> keys = new HashSet<>();
        for (int page = 1; page <= pageCount; page++) {
            keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.PAGE, page));
        }
        return keys;
    }
}
