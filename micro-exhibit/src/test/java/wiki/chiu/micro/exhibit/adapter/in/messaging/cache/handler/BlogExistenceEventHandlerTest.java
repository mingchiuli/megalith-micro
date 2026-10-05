package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.PageCacheEviction;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;

class BlogExistenceEventHandlerTest {

    private static final long BLOG_ID = 7L;

    private static final long REVISION = 3L;

    private final BlogEventRevisionGuard revisionGuard = mock(BlogEventRevisionGuard.class);
    private final PageCacheEviction cacheKeys = mock(PageCacheEviction.class);
    private final CacheEvictor cacheEvictor = mock(CacheEvictor.class);
    private final BlogExistenceService existence = mock(BlogExistenceService.class);
    private final BlogReadStateStore readStateStore = mock(BlogReadStateStore.class);
    private final CacheKeyFactory cacheKeyFactory = mock(CacheKeyFactory.class);

    @BeforeEach
    void runTheGuardedChange() {
        doAnswer(
                invocation -> {
                    invocation.getArgument(2, Runnable.class).run();
                    return null;
                })
            .when(revisionGuard)
            .applyIfNewer(anyLong(), anyLong(), any(Runnable.class));
        when(cacheKeyFactory.generate(any(), any())).thenReturn("key");
    }

    @Test
    void createAndRecoveryMarkBlogPresent() {
        CreateBlogCacheEvictHandler handler =
            new CreateBlogCacheEvictHandler(revisionGuard, cacheKeys, cacheEvictor, existence);

        handler.process(message(BlogOperateEnum.CREATE, BlogStatusEnum.NORMAL));

        verify(existence).markPresent(BLOG_ID);
        verify(cacheKeys).evict();
        verify(revisionGuard).applyIfNewer(eq(BLOG_ID), eq(REVISION), any(Runnable.class));
    }

    @Test
    void removeMarksBlogAbsentAndClearsReadState() {
        DeleteBlogCacheEvictHandler handler =
            new DeleteBlogCacheEvictHandler(
                revisionGuard, cacheKeys, cacheEvictor, cacheKeyFactory, existence, readStateStore);

        handler.process(message(BlogOperateEnum.REMOVE, BlogStatusEnum.NORMAL));

        verify(existence).markAbsent(BLOG_ID);
        verify(cacheKeys).evict();
        verify(readStateStore).clearReadToken(BLOG_ID);
        verify(readStateStore).removeFromHotRead(BLOG_ID);
    }

    @Test
    void updateClearsReadTokenForPublishedBlogs() {
        UpdateBlogCacheEvictHandler handler =
            new UpdateBlogCacheEvictHandler(
                revisionGuard, cacheKeys, cacheEvictor, cacheKeyFactory, readStateStore);

        handler.process(message(BlogOperateEnum.UPDATE, BlogStatusEnum.NORMAL));

        verify(cacheKeys).evict();
        verify(readStateStore).clearReadToken(BLOG_ID);
    }

    @Test
    void updateKeepsReadTokenWhileBlogIsNotPublished() {
        UpdateBlogCacheEvictHandler handler =
            new UpdateBlogCacheEvictHandler(
                revisionGuard, cacheKeys, cacheEvictor, cacheKeyFactory, readStateStore);

        handler.process(message(BlogOperateEnum.UPDATE, BlogStatusEnum.DRAFT));

        verify(cacheKeys).evict();
        verify(readStateStore, never()).clearReadToken(anyLong());
    }

    private BlogChangedMessage message(BlogOperateEnum operation, BlogStatusEnum status) {
        LocalDateTime now = LocalDateTime.now();
        BlogSnapshot snapshot =
            new BlogSnapshot(
                BLOG_ID, 3L, "title", "description", "content", now, now, status.getCode(), "", 0L, 1L);
        return new BlogChangedMessage("event", operation.getCode(), REVISION, 3L, snapshot);
    }
}
