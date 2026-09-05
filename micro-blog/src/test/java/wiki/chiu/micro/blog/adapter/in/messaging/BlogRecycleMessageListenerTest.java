package wiki.chiu.micro.blog.adapter.in.messaging;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.rabbitmq.client.Channel;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

class BlogRecycleMessageListenerTest {

    @Test
    void removeEventUsesItsEventIdAsTheIdempotencyKeyBeforeAck() throws Exception {
        BlogRuntimeStore runtimeStore = org.mockito.Mockito.mock(BlogRuntimeStore.class);
        RetryingMessageRecoverer recoverer = org.mockito.Mockito.mock(RetryingMessageRecoverer.class);
        BlogRecycleMessageListener listener =
            new BlogRecycleMessageListener(runtimeStore, recoverer);
        Channel channel = org.mockito.Mockito.mock(Channel.class);
        Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(11L).build();
        LocalDateTime now = LocalDateTime.now();
        BlogSnapshot snapshot =
            new BlogSnapshot(9L, 42L, "title", "description", "content", now, now, 0, null, 1L, 3L);
        BlogChangedMessage event =
            new BlogChangedMessage(
                "event-7", BlogOperateEnum.REMOVE.getCode(), 3L, 42L, snapshot);

        listener.handle(event, channel, message);

        verify(runtimeStore).saveDeletedBlog(eq(42L), eq("event-7"), eq(snapshot));
        verify(channel).basicAck(11L, false);
    }

    @Test
    void cascadeDeleteWithoutOperatorSkipsRecycleBin() throws Exception {
        BlogRuntimeStore runtimeStore = org.mockito.Mockito.mock(BlogRuntimeStore.class);
        BlogRecycleMessageListener listener =
            new BlogRecycleMessageListener(
                runtimeStore,
                org.mockito.Mockito.mock(RetryingMessageRecoverer.class));
        Channel channel = org.mockito.Mockito.mock(Channel.class);
        Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(13L).build();
        LocalDateTime now = LocalDateTime.now();
        BlogSnapshot snapshot =
            new BlogSnapshot(9L, 42L, "title", "description", "content", now, now, 0, null, 1L, 3L);

        listener.handle(
            new BlogChangedMessage(
                "event-8", BlogOperateEnum.REMOVE.getCode(), 3L, null, snapshot),
            channel,
            message);

        verifyNoInteractions(runtimeStore);
        verify(channel).basicAck(13L, false);
    }
}
