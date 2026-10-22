package wiki.chiu.micro.blog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import wiki.chiu.micro.blog.adapter.out.persistence.BlogWrapper;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringStore;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.CollaborationTicketGateway;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogFixtures;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogRenderMode;

class BlogServiceImplTest {

    @Test
    void newBlogIsOwnedAndManageableByCurrentUser() {
        BlogServiceImpl service =
            new BlogServiceImpl(
                mock(BlogQueryStore.class),
                mock(BlogRuntimeStore.class),
                mock(BlogWrapper.class),
                mock(BlogSearchGateway.class),
                new BlogAccessPolicy(),
                mock(BlogAuthoringStore.class));

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
                new BlogAccessPolicy(),
                mock(BlogAuthoringStore.class));
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

    @Test
    void editorLoadsAnActiveDraftThatIsNewerThanTheCurrentPublication() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogAuthoringStore authoring = mock(BlogAuthoringStore.class);
        LocalDateTime publishedAt = LocalDateTime.of(2026, 9, 1, 10, 0);
        BlogIdentity identity =
            new BlogIdentity(
                7L,
                42L,
                publishedAt.minusDays(1),
                0L,
                21L,
                publishedAt,
                null,
                BlogAccessMode.PUBLIC,
                1L,
                2L);
        Blog published =
            BlogFixtures.builder()
                .id(7L)
                .userId(42L)
                .title("published")
                .status(0)
                .created(publishedAt.minusDays(1))
                .updated(publishedAt)
                .build();
        BlogWorkingDraft draft =
            new BlogWorkingDraft(
                1L,
                7L,
                3L,
                21L,
                "draft",
                "draft description",
                "draft body",
                "",
                BlogRenderMode.NORMAL,
                42L,
                publishedAt.plusMinutes(1),
                List.of());
        when(authoring.findIdentity(7L)).thenReturn(Optional.of(identity));
        when(authoring.findDraft(7L)).thenReturn(Optional.of(draft));
        when(blogs.findById(7L)).thenReturn(Optional.of(published));
        BlogServiceImpl service =
            new BlogServiceImpl(
                blogs,
                mock(BlogRuntimeStore.class),
                mock(BlogWrapper.class),
                mock(BlogSearchGateway.class),
                new BlogAccessPolicy(),
                authoring);

        var edit = service.findEdit(7L, 42L, List.of());

        assertEquals("draft", edit.title());
        assertEquals(3L, edit.draftRevision());
        assertTrue(edit.draftNewer());
        assertTrue(edit.hasDraft());
    }

    @Test
    void editorKeepsThePublicationWhenItsDraftIsOlder() {
        BlogQueryStore blogs = mock(BlogQueryStore.class);
        BlogAuthoringStore authoring = mock(BlogAuthoringStore.class);
        LocalDateTime publishedAt = LocalDateTime.of(2026, 9, 1, 10, 0);
        BlogIdentity identity =
            new BlogIdentity(
                7L,
                42L,
                publishedAt.minusDays(1),
                0L,
                21L,
                publishedAt,
                null,
                BlogAccessMode.PUBLIC,
                1L,
                2L);
        Blog published =
            BlogFixtures.builder()
                .id(7L)
                .userId(42L)
                .title("published")
                .status(0)
                .created(publishedAt.minusDays(1))
                .updated(publishedAt)
                .build();
        BlogWorkingDraft draft =
            new BlogWorkingDraft(
                1L,
                7L,
                2L,
                20L,
                "old draft",
                "old",
                "old body",
                "",
                BlogRenderMode.NORMAL,
                42L,
                publishedAt.minusMinutes(1),
                List.of());
        when(authoring.findIdentity(7L)).thenReturn(Optional.of(identity));
        when(authoring.findDraft(7L)).thenReturn(Optional.of(draft));
        when(blogs.findById(7L)).thenReturn(Optional.of(published));
        BlogServiceImpl service =
            new BlogServiceImpl(
                blogs,
                mock(BlogRuntimeStore.class),
                mock(BlogWrapper.class),
                mock(BlogSearchGateway.class),
                new BlogAccessPolicy(),
                authoring);

        var edit = service.findEdit(7L, 42L, List.of());

        assertEquals("published", edit.title());
        assertFalse(edit.draftNewer());
        assertTrue(edit.hasDraft());
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<Blog>> captor() {
        return ArgumentCaptor.forClass(List.class);
    }
}
