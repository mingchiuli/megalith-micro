package wiki.chiu.micro.exhibit.adapter.in.messaging.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.handler.CacheKeyRegistry;
import wiki.chiu.micro.exhibit.cache.BlogCacheDescriptors;

class PageCacheEvictionTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void retriesTheCompleteRegisteredRangeAfterPartialFailure() {
        CacheKeyRegistry registry = mock(CacheKeyRegistry.class);
        CacheEvictor evictor = mock(CacheEvictor.class);
        Set<String> keys = IntStream.rangeClosed(1, 600).mapToObj(page -> "page-" + page)
            .collect(Collectors.toSet());
        when(registry.registeredKeys(BlogCacheDescriptors.PAGE)).thenReturn(keys);
        doNothing().doThrow(new IllegalStateException("broadcast failed")).doNothing()
            .when(evictor).evict(anySet());
        PageCacheEviction eviction = new PageCacheEviction(registry, evictor);

        assertThatThrownBy(eviction::evict).hasMessage("broadcast failed");
        eviction.evict();

        ArgumentCaptor<Set<String>> batches = ArgumentCaptor.forClass((Class) Set.class);
        verify(evictor, times(5)).evict(batches.capture());
        assertThat(batches.getAllValues()).allSatisfy(batch -> assertThat(batch).hasSizeLessThanOrEqualTo(256));
        assertThat(batches.getAllValues().subList(2, 5).stream().flatMap(Set::stream).collect(Collectors.toSet()))
            .isEqualTo(keys);
    }
}
