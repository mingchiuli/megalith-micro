package wiki.chiu.micro.blog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import wiki.chiu.micro.blog.domain.BlogEntity;

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
            .thenReturn(Optional.of(BlogEntity.builder().id(7L).userId(42L).build()));
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
        BlogEntity first = BlogEntity.builder().id(7L).eventRevision(2L).build();
        BlogEntity second = BlogEntity.builder().id(8L).eventRevision(4L).build();
        when(blogs.findByUserIds(List.of(42L))).thenReturn(List.of(first, second));
        when(blogs.count()).thenReturn(10L);
        BlogServiceImpl service =
            new BlogServiceImpl(
                blogs,
                mock(BlogRuntimeStore.class),
                writer,
                mock(BlogSearchGateway.class),
                new BlogAccessPolicy());
        ArgumentCaptor<BlogEventContext> event = ArgumentCaptor.forClass(BlogEventContext.class);

        service.deleteByUserIds(List.of(42L));

        verify(writer)
            .deleteByIds(eq(List.of(first, second)), eq(List.of()), event.capture());
        assertEquals(null, event.getValue().operatorUserId());
        assertEquals(8L, event.getValue().totalCount());
        assertEquals(10L, event.getValue().previousTotalCount());
        assertEquals(3L, first.getEventRevision());
        assertEquals(5L, second.getEventRevision());
    }
}
