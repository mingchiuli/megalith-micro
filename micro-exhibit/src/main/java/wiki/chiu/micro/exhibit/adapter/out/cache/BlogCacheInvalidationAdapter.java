package wiki.chiu.micro.exhibit.adapter.out.cache;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.exhibit.application.port.out.BlogCacheInvalidation;
import wiki.chiu.micro.exhibit.domain.BlogCacheDescriptors;

@Component
public class BlogCacheInvalidationAdapter implements BlogCacheInvalidation {

    private static final CacheDescriptor DETAIL =
        new CacheDescriptor(BlogCacheDescriptors.DETAIL_NAMESPACE, BlogCacheDescriptors.VERSION);
    private static final CacheDescriptor SENSITIVE =
        new CacheDescriptor(BlogCacheDescriptors.SENSITIVE_NAMESPACE, BlogCacheDescriptors.VERSION);

    private final CacheEvictor cacheEvictor;

    private final CacheKeyFactory cacheKeyFactory;

    private final PageCacheEviction pageCacheEviction;

    public BlogCacheInvalidationAdapter(
        CacheEvictor cacheEvictor,
        CacheKeyFactory cacheKeyFactory,
        PageCacheEviction pageCacheEviction) {
        this.cacheEvictor = cacheEvictor;
        this.cacheKeyFactory = cacheKeyFactory;
        this.pageCacheEviction = pageCacheEviction;
    }

    @Override
    public void invalidateBlog(Long blogId) {
        Set<String> keys = new HashSet<>();
        keys.add(cacheKeyFactory.generate(DETAIL, blogId));
        keys.add(cacheKeyFactory.generate(SENSITIVE, blogId));
        cacheEvictor.evict(keys);
    }

    @Override
    public void invalidatePages() {
        pageCacheEviction.evict();
    }
}
