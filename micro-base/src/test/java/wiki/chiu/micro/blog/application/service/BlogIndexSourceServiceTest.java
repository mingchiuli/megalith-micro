package wiki.chiu.micro.blog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.common.model.BlogSnapshot;

class BlogIndexSourceServiceTest {

    @Test
    void snapshotsRejectAnUnreadySourceBeforeReadingBlogs() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogIndexSourceState state = mock(BlogIndexSourceState.class);
        var service = new BlogIndexSourceService(blogs, state);
        when(state.ready()).thenReturn(false);
        assertThatThrownBy(() -> service.snapshots(0, 500)).hasMessageContaining("empty BLOG outbox");
        verifyNoInteractions(blogs);
    }

    @Test
    void snapshotsIncludeThePersistedBusinessRevision() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogIndexSourceState state = mock(BlogIndexSourceState.class);
        when(state.ready()).thenReturn(true);
        when(blogs.findPublishedSnapshotsAfter(0, 500))
            .thenReturn(
                List.of(
                    new BlogSnapshot(
                        7L,
                        42L,
                        "title",
                        "description",
                        "content",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0,
                        "",
                        120L,
                        11L)));

        var snapshot = new BlogIndexSourceService(blogs, state).snapshots(0, 500).getFirst();

        assertThat(snapshot.id()).isEqualTo(7L);
        assertThat(snapshot.revision()).isEqualTo(11L);
        assertThat(snapshot.readCount()).isEqualTo(120L);
    }

}
