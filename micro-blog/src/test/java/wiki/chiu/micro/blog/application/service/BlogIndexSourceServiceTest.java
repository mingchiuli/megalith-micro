package wiki.chiu.micro.blog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.BlogEntity;

class BlogIndexSourceServiceTest {

    @Test
    void snapshotsRequireBothReadOnlyModeAndAnEmptyOutbox() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogIndexSourceState state = mock(BlogIndexSourceState.class);
        var service = new BlogIndexSourceService(blogs, state);
        for (var status : List.of(new BlogIndexSourceStatus(false, 0, 0, 1),
            new BlogIndexSourceStatus(true, 1, 0, 1), new BlogIndexSourceStatus(true, 0, 1, 1))) {
            when(state.status()).thenReturn(status);
            assertThatThrownBy(() -> service.snapshots(0, 500)).hasMessageContaining("empty BLOG outbox");
        }
        verifyNoInteractions(blogs);
    }

    @Test
    void snapshotsIncludeThePersistedBusinessRevision() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogIndexSourceState state = mock(BlogIndexSourceState.class);
        when(state.status()).thenReturn(new BlogIndexSourceStatus(true, 0, 0, 1));
        when(blogs.findSnapshotsAfter(0, 500)).thenReturn(List.of(
            BlogEntity.builder().id(7L).eventRevision(11L).readCount(120L).build()));

        var snapshot = new BlogIndexSourceService(blogs, state).snapshots(0, 500).getFirst();

        assertThat(snapshot.id()).isEqualTo(7L);
        assertThat(snapshot.revision()).isEqualTo(11L);
        assertThat(snapshot.readCount()).isEqualTo(120L);
    }
}
