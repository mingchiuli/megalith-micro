package wiki.chiu.micro.blog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.port.out.BlogAuthoringReader;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.CollaborationRoomInitializer;
import wiki.chiu.micro.blog.application.port.out.CollaborationTicketGateway;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.common.enums.BlogAccessMode;

class BlogCollaborationServiceTest {
    private final BlogQueryStore blogs = mock(BlogQueryStore.class);
    private final BlogAuthoringReader reader = mock(BlogAuthoringReader.class);
    private final CollaborationRoomInitializer rooms = mock(CollaborationRoomInitializer.class);
    private final CollaborationTicketGateway tickets = mock(CollaborationTicketGateway.class);
    private final BlogAccessPolicy policy = new BlogAccessPolicy();
    private final BlogCollaborationServiceImpl service = new BlogCollaborationServiceImpl(
        blogs, policy, mock(BlogRuntimeStore.class), tickets, new BlogEditor(blogs, reader, policy), rooms);

    @Test
    void newUnpublishedArticlesUseIndependentRoomsAndInitializeBeforeSigning() {
        String firstEpoch = UUID.randomUUID().toString();
        String secondEpoch = UUID.randomUUID().toString();
        when(reader.findIdentity(7L)).thenReturn(Optional.of(identity(7L, firstEpoch)));
        when(reader.findIdentity(8L)).thenReturn(Optional.of(identity(8L, secondEpoch)));
        when(tickets.issueTicket(anyString(), anyLong())).thenReturn("Bearer ticket");
        long deadline = System.currentTimeMillis() + 45_000;
        var first = service.issueWebSocketTicket(7L, 42L, List.of(), deadline);
        var second = service.issueWebSocketTicket(8L, 42L, List.of(), deadline);
        assertThat(first.roomId()).isNotEqualTo(second.roomId());
        assertThat(first.roomId()).isEqualTo("blog:7:" + firstEpoch);
        assertThat(first.expiresAt()).isLessThanOrEqualTo(deadline);
        var ordered = inOrder(rooms, tickets);
        ordered.verify(rooms).initialize(first.roomId(), "");
        ordered.verify(tickets).issueTicket(first.roomId(), first.expiresAt());
    }

    @Test
    void expiredAuthorizationNeverInitializesOrSigns() {
        assertThatThrownBy(() -> service.issueWebSocketTicket(7L, 42L, List.of(), System.currentTimeMillis() - 1))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(rooms, tickets, reader);
    }

    private BlogIdentity identity(long id, String epoch) {
        return new BlogIdentity(id, 42L, LocalDateTime.now(), 0L, null, null, null,
            BlogAccessMode.PUBLIC, 0L, 0L, epoch);
    }
}
