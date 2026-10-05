package wiki.chiu.micro.blog.adapter.in.messaging;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.application.port.in.BlogRecycleBin;
import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;

@Component
public class BlogRecycleMessageListener {

    private final BlogRecycleBin recycleBin;
    private final RetryingMessageRecoverer recoverer;

    public BlogRecycleMessageListener(
        BlogRecycleBin recycleBin,
        @Qualifier("recycleMessageRecoverer") RetryingMessageRecoverer recoverer) {
        this.recycleBin = recycleBin;
        this.recoverer = recoverer;
    }

    @RabbitListener(
        queues = Const.RECYCLE_QUEUE,
        concurrency = "2",
        messageConverter = "jsonMessageConverter",
        executor = "mqExecutor")
    public void handle(BlogChangedMessage event, Channel channel, Message message) {
        try {
            recycleBin.recycle(event);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(message, channel, failure);
        }
    }
}
