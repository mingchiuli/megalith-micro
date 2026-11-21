package wiki.chiu.micro.exhibit.adapter.out.composite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.api.vo.BlogPublicationContentRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogPublicationRpcVo;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.RemoteServiceException;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.exhibit.adapter.out.http.search.SearchHttpService;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

class BlogWrapperTest {

    private final BlogHttpServiceWrapper blogs = mock(BlogHttpServiceWrapper.class);
    private final UserHttpServiceWrapper users = mock(UserHttpServiceWrapper.class);
    private final SearchHttpService search = mock(SearchHttpService.class);
    private final BlogWrapper reader = new BlogWrapper(search, blogs, users, Runnable::run);
    private final LocalDateTime created = LocalDateTime.of(2026, 1, 1, 12, 0);
    private final BlogPublicationRpcVo first = new BlogPublicationRpcVo(
        10L, 3L, BlogAccessMode.PUBLIC, created, null, 4L);
    private final RemoteServiceException conflict =
        new RemoteServiceException(CommonErrorCode.CONFLICT, "published version changed", 409);

    @Test
    void refreshesMetadataAndRendersOnlyTheNewVersionAfterAConcurrentPublication() {
        var next = new BlogPublicationRpcVo(11L, 3L, BlogAccessMode.RESTRICTED, created, created.plusDays(1), 5L);
        when(blogs.publication(7L)).thenReturn(first, next);
        when(blogs.publicationContent(7L, 10L)).thenThrow(conflict);
        when(blogs.publicationContent(7L, 11L)).thenReturn(
            new BlogPublicationContentRpcVo("title", "summary", "new", BlogRenderMode.NORMAL, List.of()));
        when(users.findById(3L)).thenReturn(UserEntityRpcVo.builder().nickname("author").build());

        var detail = reader.findById(7L);
        assertThat(detail.content()).isEqualTo("new");
        assertThat(detail.created()).isEqualTo(next.publishedAt());
        assertThat(detail.readCount()).isEqualTo(5L);
        assertThat(detail.status()).isEqualTo(BlogStatusEnum.HIDE.getCode());
        verifyNoInteractions(search);
    }

    @Test
    void limitsRetriesToOneWhenThePublicationKeepsChanging() {
        when(blogs.publication(7L)).thenReturn(first);
        when(blogs.publicationContent(7L, 10L)).thenThrow(conflict);
        assertThatThrownBy(() -> reader.findById(7L)).isSameAs(conflict);
        verify(blogs, times(2)).publication(7L);
        verifyNoInteractions(users, search);
    }
}
