package wiki.chiu.micro.blog.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import wiki.chiu.micro.blog.application.model.RecycleBlogCommand;
import wiki.chiu.micro.blog.application.port.in.BlogRecycleBin;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;

class BlogRecycleMessageListenerTest {

    private final BlogRecycleBin recycleBin = mock(BlogRecycleBin.class);
    private final RetryingMessageRecoverer recoverer = mock(RetryingMessageRecoverer.class);
    private final BlogRecycleMessageListener listener = new BlogRecycleMessageListener(recycleBin, recoverer);

    @Test
    void translatesAllRecycleStateBeforeAcknowledging() throws Exception {
        Channel channel = mock(Channel.class);
        Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(11L).build();
        BlogChangedMessage event = event("event-7", 42L);
        listener.handle(event, channel, message);

        var command = ArgumentCaptor.forClass(RecycleBlogCommand.class);
        var ordered = inOrder(recycleBin, channel);
        ordered.verify(recycleBin).recycle(command.capture());
        ordered.verify(channel).basicAck(11L, false);
        assertThat(command.getValue().eventId()).isEqualTo("event-7");
        assertThat(command.getValue().operation()).isEqualTo(BlogOperateEnum.REMOVE);
        assertThat(command.getValue().operatorUserId()).isEqualTo(42L);
        var blog = command.getValue().blog();
        assertThat(blog.id()).isEqualTo(9L);
        assertThat(blog.userId()).isEqualTo(42L);
        assertThat(blog.title()).isEqualTo("title");
        assertThat(blog.description()).isEqualTo("description");
        assertThat(blog.content()).isEqualTo("content");
        assertThat(blog.link()).isEqualTo("cover");
        assertThat(blog.status()).isEqualTo(2);
        assertThat(blog.readCount()).isEqualTo(1L);
        assertThat(blog.eventRevision()).isEqualTo(3L);
        assertThat(blog.adminRevision()).isEqualTo(7L);
        assertThat(blog.created()).isEqualTo(event.blogSnapshot().created());
        assertThat(blog.updated()).isEqualTo(event.blogSnapshot().updated());
        assertThat(blog.sensitiveContent()).containsExactly(new BlogSensitiveSpan(1, 3, 3));
    }

    @Test
    void recoversWhenTheRecycleBinFails() throws Exception {
        Channel channel = mock(Channel.class);
        Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(13L).build();
        RuntimeException failure = new IllegalStateException("boom");
        doThrow(failure).when(recycleBin).recycle(any());
        listener.handle(event("event-8", 42L), channel, message);
        verify(recoverer).recover(message, channel, failure);
        verify(channel, never()).basicAck(13L, false);
    }

    @Test
    void unknownWireOperationsAreRecoveredWithoutCallingTheUseCase() throws Exception {
        Channel channel = mock(Channel.class);
        Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(14L).build();
        var original = event("event-9", 42L);
        listener.handle(new BlogChangedMessage("event-9", 99, 3L, 42L, original.blogSnapshot()), channel, message);
        verify(recoverer).recover(eq(message), eq(channel), any(MissException.class));
        verifyNoInteractions(recycleBin);
        verify(channel, never()).basicAck(14L, false);
    }

    private BlogChangedMessage event(String eventId, Long operatorUserId) {
        LocalDateTime now = LocalDateTime.now();
        BlogSnapshot snapshot = new BlogSnapshot(9L, 42L, "title", "description", "content", now, now.plusHours(1),
            2, "cover", 1L, 3L, null, null, null, null, null, List.of(new BlogSensitiveSpan(1, 3, 3)));
        return new BlogChangedMessage(eventId, BlogOperateEnum.REMOVE.getCode(), 3L, operatorUserId, snapshot, 7L);
    }
}
