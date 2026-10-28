package wiki.chiu.micro.search.adapter.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.api.BlogIndexSourceHttpService;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.search.application.model.BlogIndexChange;

class BlogBootstrapIndexSourceHttpAdapterTest {

    @Test
    void publicStartupSnapshotsUseTheRepairOperation() {
        BlogIndexSourceHttpService blogs = mock(BlogIndexSourceHttpService.class);
        LocalDateTime date = LocalDateTime.of(2026, 10, 28, 10, 0);
        BlogSnapshot snapshot =
            new BlogSnapshot(
                7L,
                42L,
                "title",
                "description",
                "content",
                date,
                date,
                0,
                "https://cdn.example/7.png",
                10L,
                3L);
        when(blogs.indexBootstrapSnapshots(0, 500))
            .thenReturn(Result.success(List.of(snapshot)));

        BlogIndexChange change =
            new BlogBootstrapIndexSourceHttpAdapter(blogs).publicSnapshots(0, 500).getFirst();

        assertThat(change.operation()).isEqualTo(BlogIndexChange.Operation.REPAIR);
        assertThat(change.revision()).isEqualTo(3L);
        assertThat(change.blog().link()).isEqualTo("https://cdn.example/7.png");
    }
}
