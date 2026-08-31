package wiki.chiu.micro.blog.adapter.in.messaging;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.UserDeletedMessage;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

@Component
public class UserDeletedMessageListener {

    private final BlogService blogService;
    private final RetryingMessageRecoverer recoverer;

    public UserDeletedMessageListener(
        BlogService blogService,
        @Qualifier("userDeletionMessageRecoverer") RetryingMessageRecoverer recoverer) {
        this.blogService = blogService;
        this.recoverer = recoverer;
    }

    @RabbitListener(
        queues = Const.USER_DELETED_QUEUE,
        concurrency = "2",
        messageConverter = "jsonMessageConverter",
        executor = "mqExecutor")
    public void handle(UserDeletedMessage event, Channel channel, Message message) {
        try {
            blogService.deleteByUserIds(event.userIds());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(message, channel, failure);
        }
    }
}
