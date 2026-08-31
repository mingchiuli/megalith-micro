package wiki.chiu.micro.blog.adapter.in.messaging;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

@Component
public class BlogRecycleMessageListener {

    private final BlogRuntimeStore runtimeStore;
    private final RetryingMessageRecoverer recoverer;

    public BlogRecycleMessageListener(
        BlogRuntimeStore runtimeStore,
        @Qualifier("recycleMessageRecoverer") RetryingMessageRecoverer recoverer) {
        this.runtimeStore = runtimeStore;
        this.recoverer = recoverer;
    }

    @RabbitListener(
        queues = Const.RECYCLE_QUEUE,
        concurrency = "2",
        messageConverter = "jsonMessageConverter",
        executor = "mqExecutor")
    public void handle(BlogChangedMessage event, Channel channel, Message message) {
        try {
            if (BlogOperateEnum.REMOVE.equals(BlogOperateEnum.of(event.operation()))) {
                if (event.operatorUserId() == null) {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    return;
                }
                runtimeStore.saveDeletedBlog(
                    event.operatorUserId(), event.eventId(), event.blogSnapshot());
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(message, channel, failure);
        }
    }
}
