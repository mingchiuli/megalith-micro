package wiki.chiu.micro.blog.adapter.in.messaging;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.rabbitmq.client.Channel;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import wiki.chiu.micro.blog.application.port.in.BlogRecycleBin;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.common.model.BlogSnapshot;

class BlogRecycleMessageListenerTest {

    private final BlogRecycleBin recycleBin = mock(BlogRecycleBin.class);
    private final RetryingMessageRecoverer recoverer = mock(RetryingMessageRecoverer.class);
    private final BlogRecycleMessageListener listener =
        new BlogRecycleMessageListener(recycleBin, recoverer);

    @Test
    void acksAfterTheRecycleBinHandledTheEvent() throws Exception {
        Channel channel = mock(Channel.class);
        Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(11L).build();
        BlogChangedMessage event = event("event-7", 42L);

        listener.handle(event, channel, message);

        verify(recycleBin).recycle(event);
        verify(channel).basicAck(11L, false);
    }

    @Test
    void recoversWhenTheRecycleBinFails() throws Exception {
        Channel channel = mock(Channel.class);
        Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(13L).build();
        BlogChangedMessage event = event("event-8", 42L);
        RuntimeException failure = new IllegalStateException("boom");
        doThrow(failure).when(recycleBin).recycle(event);

        listener.handle(event, channel, message);

        verify(recoverer).recover(message, channel, failure);
    }

    private BlogChangedMessage event(String eventId, Long operatorUserId) {
        LocalDateTime now = LocalDateTime.now();
        BlogSnapshot snapshot =
            new BlogSnapshot(9L, 42L, "title", "description", "content", now, now, 0, null, 1L, 3L);
        return new BlogChangedMessage(
            eventId, BlogOperateEnum.REMOVE.getCode(), 3L, operatorUserId, snapshot);
    }
}
