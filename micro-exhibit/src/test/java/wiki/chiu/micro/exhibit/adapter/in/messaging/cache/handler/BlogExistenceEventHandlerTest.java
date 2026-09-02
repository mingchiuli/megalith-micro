package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;

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
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.CacheKeyGenerator;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;

class BlogExistenceEventHandlerTest {

    private final RedissonClient redisson = mock(RedissonClient.class);
    private final CacheKeyGenerator cacheKeys = mock(CacheKeyGenerator.class);
    private final CacheEvictor cacheEvictor = mock(CacheEvictor.class);
    private final BlogExistenceService existence = mock(BlogExistenceService.class);

    @Test
    void createAndRecoveryMarkBlogPresent() {
        when(cacheKeys.generateHotBlogsKeys(1L)).thenReturn(new HashSet<>());
        CreateBlogCacheEvictHandler handler =
            new CreateBlogCacheEvictHandler(redisson, cacheKeys, cacheEvictor, existence);

        handler.redisProcess(message(BlogOperateEnum.CREATE, 1L, null));

        verify(existence).markPresent(7L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeMarksBlogAbsent() {
        CacheKeyFactory cacheKeyFactory = mock(CacheKeyFactory.class);
        RKeys redisKeys = mock(RKeys.class);
        RScoredSortedSet<String> hotRead = mock(RScoredSortedSet.class);
        when(cacheKeys.generateHotBlogsKeys(2L)).thenReturn(new HashSet<>());
        when(redisson.getKeys()).thenReturn(redisKeys);
        when(redisson.<String>getScoredSortedSet(Const.HOT_READ)).thenReturn(hotRead);
        DeleteBlogCacheEvictHandler handler =
            new DeleteBlogCacheEvictHandler(
                redisson, cacheKeys, cacheEvictor, cacheKeyFactory, existence);

        handler.redisProcess(message(BlogOperateEnum.REMOVE, 1L, 2L));

        verify(existence).markAbsent(7L);
    }

    private BlogChangedMessage message(
        BlogOperateEnum operation, long totalCount, Long previousTotalCount) {
        LocalDateTime now = LocalDateTime.now();
        BlogSnapshot snapshot =
            new BlogSnapshot(
                7L, 3L, "title", "description", "content", now, now, 0, "", 0L, 1L);
        return new BlogChangedMessage(
            "event", operation.getCode(), 1L, 3L, snapshot, totalCount, 0L, previousTotalCount);
    }
}
