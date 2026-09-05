package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.PageCacheEviction;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;

class BlogExistenceEventHandlerTest {

    private final RedissonClient redisson = mock(RedissonClient.class);
    private final PageCacheEviction cacheKeys = mock(PageCacheEviction.class);
    private final CacheEvictor cacheEvictor = mock(CacheEvictor.class);
    private final BlogExistenceService existence = mock(BlogExistenceService.class);

    @Test
    void createAndRecoveryMarkBlogPresent() {
        CreateBlogCacheEvictHandler handler =
            new CreateBlogCacheEvictHandler(redisson, cacheKeys, cacheEvictor, existence);

        handler.redisProcess(message(BlogOperateEnum.CREATE));

        verify(existence).markPresent(7L);
        verify(cacheKeys).evict();
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeMarksBlogAbsent() {
        CacheKeyFactory cacheKeyFactory = mock(CacheKeyFactory.class);
        RKeys redisKeys = mock(RKeys.class);
        RScoredSortedSet<String> hotRead = mock(RScoredSortedSet.class);
        when(redisson.getKeys()).thenReturn(redisKeys);
        when(redisson.<String>getScoredSortedSet(Const.HOT_READ)).thenReturn(hotRead);
        DeleteBlogCacheEvictHandler handler =
            new DeleteBlogCacheEvictHandler(
                redisson, cacheKeys, cacheEvictor, cacheKeyFactory, existence);

        handler.redisProcess(message(BlogOperateEnum.REMOVE));

        verify(existence).markAbsent(7L);
        verify(cacheKeys).evict();
    }

    private BlogChangedMessage message(BlogOperateEnum operation) {
        LocalDateTime now = LocalDateTime.now();
        BlogSnapshot snapshot =
            new BlogSnapshot(
                7L, 3L, "title", "description", "content", now, now, 0, "", 0L, 1L);
        return new BlogChangedMessage(
            "event", operation.getCode(), 1L, 3L, snapshot);
    }
}
