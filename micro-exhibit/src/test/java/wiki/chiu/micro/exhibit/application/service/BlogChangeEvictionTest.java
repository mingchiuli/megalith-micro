package wiki.chiu.micro.exhibit.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.out.BlogCacheInvalidation;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;

class BlogChangeEvictionTest {

    private static final long BLOG_ID = 7L;

    private static final long REVISION = 3L;

    private final BlogEventRevisionGuard revisionGuard = mock(BlogEventRevisionGuard.class);
    private final BlogCacheInvalidation cacheInvalidation = mock(BlogCacheInvalidation.class);
    private final BlogExistenceStore existence = mock(BlogExistenceStore.class);
    private final BlogReadStateStore readStateStore = mock(BlogReadStateStore.class);

    private BlogChangeEvictionServiceImpl eviction;

    @BeforeEach
    void setUp() {
        doAnswer(
                invocation -> {
                    invocation.getArgument(2, Runnable.class).run();
                    return null;
                })
            .when(revisionGuard)
            .applyIfNewer(anyLong(), anyLong(), any(Runnable.class));
        eviction =
            new BlogChangeEvictionServiceImpl(
                List.of(
                    new CreateBlogChangeEvictionHandler(revisionGuard, cacheInvalidation, existence),
                    new UpdateBlogChangeEvictionHandler(
                        revisionGuard, cacheInvalidation, readStateStore),
                    new DeleteBlogChangeEvictionHandler(
                        revisionGuard, cacheInvalidation, readStateStore, existence)));
    }

    @Test
    void createEventEvictsPagesAndMarksBlogPresent() {
        eviction.apply(message(BlogOperateEnum.CREATE, BlogStatusEnum.NORMAL));

        verify(cacheInvalidation).invalidatePages();
        verify(existence).markPresent(BLOG_ID);
        verify(revisionGuard).applyIfNewer(eq(BLOG_ID), eq(REVISION), any(Runnable.class));
    }

    @Test
    void updateEventEvictsBlogAndPagesAndClearsReadTokenForPublishedBlogs() {
        eviction.apply(message(BlogOperateEnum.UPDATE, BlogStatusEnum.NORMAL));

        verify(cacheInvalidation).invalidateBlog(BLOG_ID);
        verify(cacheInvalidation).invalidatePages();
        verify(readStateStore).clearReadToken(BLOG_ID);
    }

    @Test
    void updateEventKeepsReadTokenWhileBlogIsNotPublished() {
        eviction.apply(message(BlogOperateEnum.UPDATE, BlogStatusEnum.DRAFT));

        verify(cacheInvalidation).invalidateBlog(BLOG_ID);
        verify(cacheInvalidation).invalidatePages();
        verify(readStateStore, never()).clearReadToken(anyLong());
    }

    @Test
    void removeEventInvalidatesBlogStateAndExistence() {
        eviction.apply(message(BlogOperateEnum.REMOVE, BlogStatusEnum.NORMAL));

        verify(cacheInvalidation).invalidateBlog(BLOG_ID);
        verify(cacheInvalidation).invalidatePages();
        verify(readStateStore).clearReadToken(BLOG_ID);
        verify(existence).markAbsent(BLOG_ID);
        verify(readStateStore).removeFromHotRead(BLOG_ID);
    }

    @Test
    void duplicateOperationHandlersAreRejected() {
        var handler = new CreateBlogChangeEvictionHandler(revisionGuard, cacheInvalidation, existence);
        assertThatThrownBy(() -> new BlogChangeEvictionServiceImpl(List.of(handler, handler)))
            .isInstanceOf(IllegalStateException.class);
    }

    private BlogChange message(BlogOperateEnum operation, BlogStatusEnum status) {
        return new BlogChange(BLOG_ID, operation, REVISION, status.getCode());
    }
}
