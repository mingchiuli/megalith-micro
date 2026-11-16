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
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;

class BlogChangeMaintenanceTest {

    private static final long BLOG_ID = 7L;

    private static final long REVISION = 3L;

    private final BlogEventRevisionGuard revisionGuard = mock(BlogEventRevisionGuard.class);
    private final BlogExistenceStore existence = mock(BlogExistenceStore.class);
    private final BlogReadStateStore readStateStore = mock(BlogReadStateStore.class);

    private BlogChangeMaintenanceServiceImpl maintenance;

    @BeforeEach
    void setUp() {
        doAnswer(
                invocation -> {
                    invocation.getArgument(2, Runnable.class).run();
                    return null;
                })
            .when(revisionGuard)
            .applyIfNewer(anyLong(), anyLong(), any(Runnable.class));
        maintenance =
            new BlogChangeMaintenanceServiceImpl(
                List.of(
                    new CreateBlogChangeMaintenanceHandler(revisionGuard, existence),
                    new UpdateBlogChangeMaintenanceHandler(
                        revisionGuard, readStateStore),
                    new DeleteBlogChangeMaintenanceHandler(
                        revisionGuard, readStateStore, existence)));
    }

    @Test
    void createEventMarksBlogPresent() {
        maintenance.apply(message(BlogOperateEnum.CREATE, BlogStatusEnum.NORMAL));

        verify(existence).markPresent(BLOG_ID);
        verify(revisionGuard).applyIfNewer(eq(BLOG_ID), eq(REVISION), any(Runnable.class));
    }

    @Test
    void updateEventClearsReadTokenForPublishedBlogs() {
        maintenance.apply(message(BlogOperateEnum.UPDATE, BlogStatusEnum.NORMAL));

        verify(readStateStore).clearReadToken(BLOG_ID);
    }

    @Test
    void updateEventKeepsReadTokenWhileBlogIsNotPublished() {
        maintenance.apply(message(BlogOperateEnum.UPDATE, BlogStatusEnum.DRAFT));

        verify(readStateStore, never()).clearReadToken(anyLong());
    }

    @Test
    void removeEventInvalidatesBlogStateAndExistence() {
        maintenance.apply(message(BlogOperateEnum.REMOVE, BlogStatusEnum.NORMAL));

        verify(readStateStore).clearReadToken(BLOG_ID);
        verify(existence).markAbsent(BLOG_ID);
        verify(readStateStore).removeFromHotRead(BLOG_ID);
    }

    @Test
    void duplicateOperationHandlersAreRejected() {
        var handler = new CreateBlogChangeMaintenanceHandler(revisionGuard, existence);
        assertThatThrownBy(() -> new BlogChangeMaintenanceServiceImpl(List.of(handler, handler)))
            .isInstanceOf(IllegalStateException.class);
    }

    private BlogChange message(BlogOperateEnum operation, BlogStatusEnum status) {
        return new BlogChange(BLOG_ID, operation, REVISION, status.getCode());
    }
}
