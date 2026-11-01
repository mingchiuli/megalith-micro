package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.model.BlogDraftWriteCommand;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;

class JpaBlogAuthoringAdapterTest {

    private final BlogRepository blogs = mock(BlogRepository.class);
    private final BlogRevisionRepository revisions = mock(BlogRevisionRepository.class);
    private final BlogDraftRepository drafts = mock(BlogDraftRepository.class);
    private final BlogRevisionSensitiveContentRepository revisionSensitive =
        mock(BlogRevisionSensitiveContentRepository.class);
    private final BlogDraftSensitiveContentRepository draftSensitive =
        mock(BlogDraftSensitiveContentRepository.class);
    private final OutboxEvents outbox = mock(OutboxEvents.class);
    private final JpaBlogAuthoringAdapter writer =
        new JpaBlogAuthoringAdapter(
            blogs, revisions, drafts, revisionSensitive, draftSensitive, outbox);

    @Test
    void draftWriteUsesConditionalRevisionUpdateAndDoesNotReadInsideTheWriter() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 1, 10, 0);
        when(blogs.bumpAdminRevision(7L, 4L)).thenReturn(1);
        when(drafts.save(any(BlogDraftEntity.class)))
            .thenAnswer(
                call -> {
                    BlogDraftEntity entity = call.getArgument(0);
                    return new BlogDraftEntity(
                        11L,
                        entity.getBlogId(),
                        entity.getDraftRevision(),
                        entity.getBasePublishedRevisionId(),
                        entity.getTitle(),
                        entity.getDescription(),
                        entity.getContent(),
                        entity.getLink(),
                        entity.getRenderMode(),
                        entity.getUpdatedBy(),
                        entity.getUpdatedAt());
                });

        var result =
            writer.saveDraft(
                new BlogDraftWriteCommand(
                    identity(4L),
                    null,
                    new BlogContentCommand(
                        "title", "description", "content", "", BlogRenderMode.NORMAL, List.of()),
                    null,
                    42L,
                    now));

        assertThat(result.draftRevision()).isEqualTo(1L);
        assertThat(result.updatedAt()).isEqualTo(now);
        verify(blogs).bumpAdminRevision(7L, 4L);
        verifyNoMoreInteractions(blogs);
    }

    private static BlogIdentity identity(long adminRevision) {
        LocalDateTime now = LocalDateTime.of(2026, 11, 1, 9, 0);
        return new BlogIdentity(
            7L,
            42L,
            now,
            0L,
            21L,
            now,
            null,
            BlogAccessMode.PUBLIC,
            3L,
            adminRevision);
    }
}
