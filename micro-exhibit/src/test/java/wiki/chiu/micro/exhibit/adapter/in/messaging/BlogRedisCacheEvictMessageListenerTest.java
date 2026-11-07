package wiki.chiu.micro.exhibit.adapter.in.messaging;

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

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.in.BlogChangeEviction;

class BlogRedisCacheEvictMessageListenerTest {

    private final BlogChangeEviction eviction = mock(BlogChangeEviction.class);
    private final RetryingMessageRecoverer recoverer = mock(RetryingMessageRecoverer.class);
    private final BlogRedisCacheEvictMessageListener listener = new BlogRedisCacheEvictMessageListener(eviction, recoverer);
    private final Channel channel = mock(Channel.class);
    private final Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(11L).build();

    @Test
    void translatesWireFieldsAndAcknowledgesAfterTheUseCase() throws Exception {
        listener.handler(event(BlogOperateEnum.UPDATE.getCode()), channel, message);
        var change = ArgumentCaptor.forClass(BlogChange.class);
        var ordered = inOrder(eviction, channel);
        ordered.verify(eviction).apply(change.capture());
        ordered.verify(channel).basicAck(11L, false);
        assertThat(change.getValue()).isEqualTo(new BlogChange(7L, BlogOperateEnum.UPDATE, 3L, 2));
    }

    @Test
    void failedEvictionIsRecoveredInsteadOfAcknowledged() throws Exception {
        var failure = new IllegalStateException("eviction unavailable");
        doThrow(failure).when(eviction).apply(any());
        listener.handler(event(BlogOperateEnum.REMOVE.getCode()), channel, message);
        verify(recoverer).recover(message, channel, failure);
        verify(channel, never()).basicAck(11L, false);
    }

    @Test
    void unknownWireOperationsAreRecoveredBeforeCallingTheUseCase() throws Exception {
        listener.handler(event(99), channel, message);
        verify(recoverer).recover(eq(message), eq(channel), any(MissException.class));
        verifyNoInteractions(eviction);
        verify(channel, never()).basicAck(11L, false);
    }

    private BlogChangedMessage event(int operation) {
        var now = LocalDateTime.now();
        return new BlogChangedMessage("event", operation, 3L, 42L,
            new BlogSnapshot(7L, 42L, "title", "description", "content", now, now, 2, "cover", 1L, 3L));
    }
}
