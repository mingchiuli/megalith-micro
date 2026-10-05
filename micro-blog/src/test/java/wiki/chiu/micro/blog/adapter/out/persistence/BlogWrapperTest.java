package wiki.chiu.micro.blog.adapter.out.persistence;

import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.domain.BlogFixtures;
import wiki.chiu.micro.blog.domain.Blog;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.outbox.application.OutboxService;

class BlogWrapperTest {

    private final BlogRepository blogs = Mockito.mock(BlogRepository.class);
    private final BlogSensitiveContentRepository sensitiveContents =
        Mockito.mock(BlogSensitiveContentRepository.class);
    private final OutboxService outbox = Mockito.mock(OutboxService.class);
    private final BlogWrapper wrapper = new BlogWrapper(blogs, sensitiveContents, outbox,
        new BlogMaintenanceMode(false));

    @Test
    void maintenanceStopsAllContentWritersButAllowsViewCounting() {
        BlogWrapper readOnly = new BlogWrapper(blogs, sensitiveContents, outbox,
            new BlogMaintenanceMode(true));
        var event = new BlogEventContext(BlogOperateEnum.CREATE, 42L);
        assertThrows(BaseException.class,
            () -> readOnly.saveOrUpdate(blog(7L, 1L), null, List.of(), List.of(), event));
        assertThrows(BaseException.class, () -> readOnly.recoverDeletedBlog(blog(7L, 1L), event));
        assertThrows(BaseException.class, () -> readOnly.deleteByIds(List.of(blog(7L, 2L)), List.of(), event));
        verifyNoInteractions(blogs, sensitiveContents, outbox);

        readOnly.incrementViews(7L);
        verify(blogs).setReadCount(7L);
    }

    @Test
    void updateConflictStopsAssociationWritesAndOutbox() {
        Blog candidate = blog(7L, 2L);
        when(blogs.updateByIdAndEventRevision(
            7L,
            1L,
            2L,
            candidate.title(),
            candidate.description(),
            candidate.content(),
            candidate.status(),
            candidate.link(),
            candidate.updated()))
            .thenReturn(0);

        assertThrows(
            BaseException.class,
            () ->
                wrapper.saveOrUpdate(
                    candidate,
                    1L,
                    List.of(11L),
                    List.of(),
                    new BlogEventContext(BlogOperateEnum.UPDATE, 42L)));

        verify(sensitiveContents, never()).deleteAllByIdInBatch(List.of(11L));
        verifyNoInteractions(outbox);
    }

    @Test
    void batchDeleteConflictDoesNotEnqueueAnyEvent() {
        Blog first = blog(7L, 2L);
        Blog second = blog(8L, 4L);
        when(blogs.deleteByIdAndEventRevision(7L, 1L)).thenReturn(1);
        when(blogs.deleteByIdAndEventRevision(8L, 3L)).thenReturn(0);

        assertThrows(
            BaseException.class,
            () ->
                wrapper.deleteByIds(
                    List.of(first, second),
                    List.of(11L, 12L),
                    new BlogEventContext(BlogOperateEnum.REMOVE, 42L)));

        verify(blogs).deleteByIdAndEventRevision(7L, 1L);
        verify(blogs).deleteByIdAndEventRevision(8L, 3L);
        verify(sensitiveContents, never()).deleteAllByIdInBatch(List.of(11L, 12L));
        verifyNoInteractions(outbox);
    }

    private Blog blog(Long id, Long eventRevision) {
        return BlogFixtures.builder()
            .id(id)
            .userId(42L)
            .title("title")
            .description("description")
            .content("content")
            .created(LocalDateTime.of(2026, 8, 15, 10, 0))
            .updated(LocalDateTime.of(2026, 8, 15, 11, 0))
            .status(0)
            .link("link")
            .readCount(0L)
            .eventRevision(eventRevision)
            .build();
    }
}
