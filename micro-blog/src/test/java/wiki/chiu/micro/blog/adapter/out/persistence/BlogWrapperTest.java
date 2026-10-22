package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogFixtures;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;

class BlogWrapperTest {

    private final BlogRepository blogs = Mockito.mock(BlogRepository.class);
    private final BlogRevisionRepository revisions = Mockito.mock(BlogRevisionRepository.class);
    private final BlogDraftRepository drafts = Mockito.mock(BlogDraftRepository.class);
    private final BlogDraftSensitiveContentRepository draftSensitive =
        Mockito.mock(BlogDraftSensitiveContentRepository.class);
    private final BlogRevisionSensitiveContentRepository revisionSensitive =
        Mockito.mock(BlogRevisionSensitiveContentRepository.class);
    private final OutboxEvents outbox = Mockito.mock(OutboxEvents.class);

    @Test
    void maintenanceStopsDestructiveWritersButAllowsViewCounting() {
        BlogWrapper readOnly = wrapper(new BlogMaintenanceMode(true));
        var event = new BlogEventContext(BlogOperateEnum.CREATE, 42L);

        assertThrows(
            BaseException.class,
            () -> readOnly.recoverDeletedBlog(blog(), event));
        assertThrows(
            BaseException.class,
            () -> readOnly.deleteByIds(List.of(blog()), List.of(), event));
        verifyNoInteractions(blogs, revisions, drafts, draftSensitive, revisionSensitive, outbox);

        readOnly.incrementViews(7L);
        verify(blogs).setReadCount(7L);
    }

    private BlogWrapper wrapper(BlogMaintenanceMode mode) {
        return new BlogWrapper(
            blogs,
            revisions,
            drafts,
            draftSensitive,
            revisionSensitive,
            outbox,
            mode);
    }

    private Blog blog() {
        return BlogFixtures.builder()
            .id(7L)
            .userId(42L)
            .title("title")
            .description("description")
            .content("content")
            .created(LocalDateTime.of(2026, 8, 15, 10, 0))
            .updated(LocalDateTime.of(2026, 8, 15, 11, 0))
            .status(0)
            .link("link")
            .readCount(0L)
            .eventRevision(2L)
            .build();
    }
}
