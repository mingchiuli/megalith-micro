package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;

class JpaBlogQueryAdapterBootstrapTest {

    private final BlogRepository blogs = mock(BlogRepository.class);
    private final BlogRevisionRepository revisions = mock(BlogRevisionRepository.class);
    private final BlogDraftRepository drafts = mock(BlogDraftRepository.class);
    private final BlogRevisionSensitiveContentRepository sensitive =
        mock(BlogRevisionSensitiveContentRepository.class);
    private final JpaBlogQueryAdapter adapter =
        new JpaBlogQueryAdapter(blogs, revisions, drafts, sensitive);

    @Test
    void adminSnapshotsBatchLoadDraftAndPublishedRevision() {
        LocalDateTime created = LocalDateTime.of(2026, 10, 20, 10, 0);
        LocalDateTime updated = created.plusDays(1);
        BlogEntity blog = new BlogEntity(7L, 42L, created, 15L, 11L, updated, null, 0, 2L, 3L);
        BlogRevisionEntity revision =
            new BlogRevisionEntity(
                11L,
                7L,
                1L,
                BlogRevisionOrigin.PUBLICATION,
                null,
                "published",
                "description",
                "content",
                "https://cdn.example/7.png",
                BlogRenderMode.NORMAL,
                updated,
                42L,
                created);
        BlogDraftEntity draft =
            new BlogDraftEntity(
                5L,
                7L,
                2L,
                11L,
                "draft",
                "draft description",
                "draft content",
                "https://cdn.example/7-draft.png",
                BlogRenderMode.NORMAL,
                42L,
                updated.plusHours(1));
        when(blogs.findByIdGreaterThanOrderByIdAsc(0L, PageRequest.of(0, 2)))
            .thenReturn(List.of(blog));
        when(revisions.findAllById(List.of(11L))).thenReturn(List.of(revision));
        when(drafts.findByBlogIdIn(List.of(7L))).thenReturn(List.of(draft));
        when(sensitive.findByRevisionIdIn(List.of(11L)))
            .thenReturn(List.of(new BlogRevisionSensitiveContentEntity(1L, 11L, 2, 4, 1)));

        var snapshot = adapter.findAdminSnapshotsAfter(0, 2).getFirst();

        assertThat(snapshot.blog().id()).isEqualTo(7L);
        assertThat(snapshot.blog().publishedRevisionId()).isEqualTo(11L);
        assertThat(snapshot.draft().title()).isEqualTo("draft");
        assertThat(snapshot.revision().link()).isEqualTo("https://cdn.example/7.png");
        assertThat(snapshot.revision().sensitiveContent()).hasSize(1);
    }

    @Test
    void revisionSnapshotsUseRevisionIdCursorAndIncludeSensitiveSpans() {
        LocalDateTime date = LocalDateTime.of(2026, 10, 20, 10, 0);
        BlogRevisionEntity revision =
            new BlogRevisionEntity(
                9L,
                7L,
                2L,
                BlogRevisionOrigin.DRAFT,
                8L,
                "title",
                "description",
                "content",
                "https://cdn.example/9.png",
                BlogRenderMode.NORMAL,
                null,
                42L,
                date);
        when(revisions.findByIdGreaterThanOrderByIdAsc(8L, PageRequest.of(0, 2)))
            .thenReturn(List.of(revision));
        when(sensitive.findByRevisionIdIn(List.of(9L)))
            .thenReturn(List.of(new BlogRevisionSensitiveContentEntity(1L, 9L, 1, 3, 2)));

        var snapshot = adapter.findRevisionSnapshotsAfter(8, 2).getFirst();

        assertThat(snapshot.id()).isEqualTo(9L);
        assertThat(snapshot.baseRevisionId()).isEqualTo(8L);
        assertThat(snapshot.sensitiveContent()).hasSize(1);
    }
}
