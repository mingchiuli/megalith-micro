package wiki.chiu.micro.blog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.model.BlogDownloadQuery;
import wiki.chiu.micro.blog.application.model.BlogExportPage;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogQuery;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringStore;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.common.enums.BlogAccessMode;

class BlogSearchReadbackTest {

    private final BlogQueryStore blogs = mock(BlogQueryStore.class);
    private final BlogSearchGateway search = mock(BlogSearchGateway.class);
    private final BlogRuntimeStore runtime = mock(BlogRuntimeStore.class);
    private final BlogAuthoringStore authoring = mock(BlogAuthoringStore.class);
    private final BlogServiceImpl service = new BlogServiceImpl(
        blogs,
        runtime,
        mock(BlogWriter.class),
        search,
        new BlogAccessPolicy(),
        authoring);

    @Test
    void adminListIsServedFromEsAndOnlyDecoratedWithRecentCounts() {
        when(search.searchAdmin(any())).thenReturn(adminPage(3L, 2L, 1L));
        when(runtime.readCounts(List.of(3L, 2L, 1L))).thenReturn(Map.of(3L, 9));

        var result = service.findAllBlogs(new BlogQuery(1, 10, "", null, null, null), 42L, List.of());

        assertThat(result.content()).extracting(value -> value.id()).containsExactly(3L, 2L, 1L);
        assertThat(result.content().getFirst().recentReadCount()).isEqualTo(9);
        assertThat(result.totalElements()).isEqualTo(3);
        verify(search).searchAdmin(any());
        verifyNoInteractions(blogs);
    }

    @Test
    void anEsFailureDoesNotStartAnIndependentDatabaseQuery() {
        when(search.searchAdmin(any())).thenThrow(new IllegalStateException("es unavailable"));

        assertThatThrownBy(() -> service.findAllBlogs(
            new BlogQuery(1, 10, null, null, null, null), 42L, List.of())).hasMessage("es unavailable");

        verifyNoInteractions(blogs, runtime);
    }

    @Test
    void exportUsesEsAndDoesNotIncludeRowsOutsideTheCurrentUserScope() {
        when(search.searchAdmin(any())).thenReturn(adminPage(3L, 1L));
        when(authoring.findIdentity(3L)).thenReturn(Optional.of(identity(3L, 42L)));
        when(authoring.findIdentity(1L)).thenReturn(Optional.of(identity(1L, 42L)));
        when(authoring.findVersions(anyLong())).thenReturn(List.of());

        List<BlogExportPage> pages =
            new BlogExportServiceImpl(search, authoring)
                .pages(new BlogDownloadQuery(null, null, null, null), 42L, List.of())
                .toList();

        assertThat(pages).hasSize(1);
        assertThat(pages.getFirst().blogs())
            .extracting(row -> row.blog().id())
            .containsExactly(3L, 1L);
        assertThat(pages.getFirst().sensitiveContent()).isEmpty();
    }

    private Page<BlogListItem> adminPage(Long... ids) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return new Page<>(
            Arrays.stream(ids)
                .map(
                    id ->
                        new BlogListItem(
                            id,
                            "article-" + id,
                            "description",
                            "content",
                            "",
                            3L,
                            0,
                            date,
                            date,
                            0))
                .toList(),
            (long) ids.length,
            1,
            10,
            true,
            true,
            false,
            ids.length == 0 ? 0 : 1);
    }

    private BlogIdentity identity(long id, long userId) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return new BlogIdentity(
            id, userId, date, 0L, null, null, null, BlogAccessMode.PUBLIC, 0L, 0L);
    }
}
