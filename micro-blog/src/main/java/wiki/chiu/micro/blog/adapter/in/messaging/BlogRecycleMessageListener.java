package wiki.chiu.micro.blog.adapter.in.messaging;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.application.model.RecycleBlogCommand;
import wiki.chiu.micro.blog.application.port.in.BlogRecycleBin;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
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
            recycleBin.recycle(toCommand(event));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(message, channel, failure);
        }
    }

    private static RecycleBlogCommand toCommand(BlogChangedMessage event) {
        BlogOperateEnum operation = BlogOperateEnum.of(event.operation());
        var snapshot = event.blogSnapshot();
        Blog blog = snapshot == null ? null : new Blog(
            snapshot.id(), snapshot.userId(), snapshot.title(), snapshot.description(), snapshot.content(),
            snapshot.created(), snapshot.updated(), snapshot.status(), snapshot.link(), snapshot.readCount(),
            snapshot.revision(), event.adminRevision(), snapshot.sensitiveContent());
        return new RecycleBlogCommand(event.eventId(), operation, event.operatorUserId(), blog);
    }
}
