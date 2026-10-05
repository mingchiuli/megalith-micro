package wiki.chiu.micro.exhibit.adapter.out.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.exhibit.domain.BlogCacheDescriptors;

class BlogCacheInvalidationAdapterTest {

    private final CacheEvictor cacheEvictor = mock(CacheEvictor.class);
    private final CacheKeyFactory cacheKeyFactory = mock(CacheKeyFactory.class);
    private final PageCacheEviction pageCacheEviction = mock(PageCacheEviction.class);
    private final BlogCacheInvalidationAdapter adapter =
        new BlogCacheInvalidationAdapter(cacheEvictor, cacheKeyFactory, pageCacheEviction);

    @Test
    @SuppressWarnings("unchecked")
    void invalidateBlogEvictsDetailAndSensitiveKeys() {
        when(
                cacheKeyFactory.generate(
                    new CacheDescriptor(
                        BlogCacheDescriptors.DETAIL_NAMESPACE, BlogCacheDescriptors.VERSION),
                    7L))
            .thenReturn("detail");
        when(
                cacheKeyFactory.generate(
                    new CacheDescriptor(
                        BlogCacheDescriptors.SENSITIVE_NAMESPACE, BlogCacheDescriptors.VERSION),
                    7L))
            .thenReturn("sensitive");

        adapter.invalidateBlog(7L);

        ArgumentCaptor<Set<String>> keys = ArgumentCaptor.forClass(Set.class);
        verify(cacheEvictor).evict(keys.capture());
        assertThat(keys.getValue()).containsExactlyInAnyOrder("detail", "sensitive");
    }

    @Test
    void invalidatePagesDelegatesToPageEviction() {
        adapter.invalidatePages();

        verify(pageCacheEviction).evict();
    }
}
