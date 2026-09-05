package wiki.chiu.micro.blog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;

class BlogSearchReadbackTest {

    private final BlogQueryStore blogs = mock(BlogQueryStore.class);
    private final BlogSearchGateway search = mock(BlogSearchGateway.class);
    private final BlogRuntimeStore runtime = mock(BlogRuntimeStore.class);
    private final BlogServiceImpl service = new BlogServiceImpl(
        blogs, runtime, mock(BlogWriter.class), search, new BlogAccessPolicy());

    @Test
    void aBlankQueryUsesEsAndReadbackPreservesItsOrderAndPermissionBoundary() {
        when(search.searchBlogs(any())).thenReturn(page(List.of(3L, 2L, 1L)));
        when(blogs.findAllById(List.of(3L, 2L, 1L))).thenReturn(List.of(blog(1, 42), blog(2, 99), blog(3, 42)));

        var result = service.findAllBlogs(new BlogQueryReq(1, 10, "", null, null, null), 42L, List.of());

        assertThat(result.content()).extracting(value -> value.id()).containsExactly(3L, 1L);
        assertThat(result.totalElements()).isEqualTo(3);
        verify(search).searchBlogs(any());
        verify(blogs).findSensitiveByBlogIds(List.of(3L, 1L));
    }

    @Test
    void anEsFailureDoesNotStartAnIndependentDatabaseQuery() {
        when(search.searchBlogs(any())).thenThrow(new IllegalStateException("es unavailable"));

        assertThatThrownBy(() -> service.findAllBlogs(
            new BlogQueryReq(1, 10, null, null, null, null), 42L, List.of())).hasMessage("es unavailable");

        verifyNoInteractions(blogs, runtime);
    }

    @Test
    void exportUsesEsAndDoesNotIncludeRowsOutsideTheCurrentUserScope() {
        when(search.countBlogs(any())).thenReturn(3L);
        when(search.searchBlogs(any())).thenReturn(page(List.of(3L, 2L, 1L)));
        when(blogs.findAllById(List.of(3L, 2L, 1L))).thenReturn(List.of(blog(1, 42), blog(2, 99), blog(3, 42)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        new BlogExportServiceImpl(blogs, search).write(
            new BlogDownloadReq(null, null, null, null), 42L, List.of(), output);

        String sql = output.toString(StandardCharsets.UTF_8);
        assertThat(sql).contains("article-1", "article-3").doesNotContain("article-2");
        assertThat(sql.indexOf("article-3")).isLessThan(sql.indexOf("article-1"));
        verify(blogs).findSensitiveByBlogIds(List.of(3L, 1L));
    }

    private BlogSearchRpcVo page(List<Long> ids) {
        return BlogSearchRpcVo.builder().ids(ids).currentPage(1).size(10).total(3L).build();
    }

    private BlogEntity blog(long id, long userId) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return BlogEntity.builder().id(id).userId(userId).title("article-" + id)
            .description("description").content("content").link("").created(date).updated(date)
            .status(0).readCount(0L).eventRevision(1L).build();
    }
}
