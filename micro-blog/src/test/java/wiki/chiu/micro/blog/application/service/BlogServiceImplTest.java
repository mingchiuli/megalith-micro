package wiki.chiu.micro.blog.application.service;

import wiki.chiu.micro.blog.domain.BlogFixtures;
import wiki.chiu.micro.blog.domain.Blog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import wiki.chiu.micro.blog.adapter.out.persistence.BlogWrapper;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.CollaborationTicketGateway;

class BlogServiceImplTest {

    @Test
    void newBlogIsOwnedAndManageableByCurrentUser() {
        BlogServiceImpl service =
            new BlogServiceImpl(
                mock(BlogQueryStore.class),
                mock(BlogRuntimeStore.class),
                mock(BlogWrapper.class),
                mock(BlogSearchGateway.class),
                new BlogAccessPolicy());

        var edit = service.findEdit(null, 42L, List.of());

        assertEquals(42L, edit.userId());
        assertTrue(edit.permissions().collaborate());
        assertTrue(edit.permissions().commit());
        assertTrue(edit.permissions().manageMetadata());
        assertTrue(edit.permissions().manageAssets());
    }

    @Test
    void returnsOnlyTheOneTimeReadToken() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogRuntimeStore runtimeStore = mock(BlogRuntimeStore.class);
        when(blogs.findById(7L))
            .thenReturn(Optional.of(BlogFixtures.builder().id(7L).userId(42L).build()));
        BlogCollaborationServiceImpl service =
            new BlogCollaborationServiceImpl(
                blogs,
                new BlogAccessPolicy(),
                runtimeStore,
                mock(CollaborationTicketGateway.class));

        String token = service.issueReadToken(7L, 42L, List.of());

        assertFalse(token.contains("?token="));
        assertFalse(token.contains("/blog/"));
        verify(runtimeStore).saveReadToken(7L, token);
    }

    @Test
    void userDeletionRemovesOwnedBlogsWithoutAddingThemToAUsersRecycleBin() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogWrapper writer = mock(BlogWrapper.class);
        Blog first = BlogFixtures.builder().id(7L).eventRevision(2L).build();
        Blog second = BlogFixtures.builder().id(8L).eventRevision(4L).build();
        when(blogs.findByUserIds(List.of(42L))).thenReturn(List.of(first, second));
        BlogServiceImpl service =
            new BlogServiceImpl(
                blogs,
                mock(BlogRuntimeStore.class),
                writer,
                mock(BlogSearchGateway.class),
                new BlogAccessPolicy());
        ArgumentCaptor<BlogEventContext> event = ArgumentCaptor.forClass(BlogEventContext.class);
        ArgumentCaptor<List<Blog>> deleted = captor();

        service.deleteByUserIds(List.of(42L));

        verify(writer).deleteByIds(deleted.capture(), eq(List.of()), event.capture());
        assertEquals(null, event.getValue().operatorUserId());
        verify(blogs, never()).count();
        assertEquals(
            List.of(7L, 8L), deleted.getValue().stream().map(Blog::id).toList());
        assertEquals(
            List.of(3L, 5L), deleted.getValue().stream().map(Blog::eventRevision).toList());
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<Blog>> captor() {
        return ArgumentCaptor.forClass(List.class);
    }
}
