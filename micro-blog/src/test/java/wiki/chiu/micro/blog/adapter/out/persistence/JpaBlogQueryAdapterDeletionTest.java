package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.common.enums.BlogRenderMode;

class JpaBlogQueryAdapterDeletionTest {

    private final BlogRepository blogs = mock(BlogRepository.class);
    private final BlogRevisionRepository revisions = mock(BlogRevisionRepository.class);
    private final BlogDraftRepository drafts = mock(BlogDraftRepository.class);
    private final BlogRevisionSensitiveContentRepository sensitive =
        mock(BlogRevisionSensitiveContentRepository.class);
    private final JpaBlogQueryAdapter adapter =
        new JpaBlogQueryAdapter(blogs, revisions, drafts, sensitive);

    @Test
    void deletionTargetsIncludeDraftOnlyBlogsAndBothRevisionCounters() {
        LocalDateTime created = LocalDateTime.of(2026, 10, 20, 10, 0);
        BlogEntity blog =
            new BlogEntity(7L, 42L, created, 0L, null, null, null, 0, 0L, 5L);
        BlogDraftEntity draft =
            new BlogDraftEntity(
                1L,
                7L,
                2L,
                null,
                "draft",
                "description",
                "content",
                "https://cdn.example/7.png",
                BlogRenderMode.NORMAL,
                42L,
                created.plusHours(1));
        when(blogs.findAllById(List.of(7L))).thenReturn(List.of(blog));
        when(drafts.findByBlogId(7L)).thenReturn(Optional.of(draft));

        var target = adapter.findDeletionTargetsByIds(List.of(7L)).getFirst();

        assertThat(target.blog().title()).isEqualTo("draft");
        assertThat(target.identity().publicationRevision()).isZero();
        assertThat(target.identity().adminRevision()).isEqualTo(5L);
    }
}
