package wiki.chiu.micro.blog.adapter.in.messaging;


import com.rabbitmq.client.Channel;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.common.lang.UserDeletedMessage;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

class UserDeletedMessageListenerTest {

    @Test
    void deletesBlogsBeforeAcknowledging() throws Exception {
        BlogService blogs = org.mockito.Mockito.mock(BlogService.class);
        RetryingMessageRecoverer recoverer =
            org.mockito.Mockito.mock(RetryingMessageRecoverer.class);
        UserDeletedMessageListener listener = new UserDeletedMessageListener(blogs, recoverer);
        Channel channel = org.mockito.Mockito.mock(Channel.class);
        Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(12L).build();

        listener.handle(new UserDeletedMessage("event-1", List.of(7L, 9L)), channel, message);

        var order = org.mockito.Mockito.inOrder(blogs, channel);
        order.verify(blogs).deleteByUserIds(List.of(7L, 9L));
        order.verify(channel).basicAck(12L, false);
    }
}
