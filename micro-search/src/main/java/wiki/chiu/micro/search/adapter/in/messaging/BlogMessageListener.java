package wiki.chiu.micro.search.adapter.in.messaging;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.port.in.ApplyBlogIndexChangeUseCase;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class BlogMessageListener {

    private final ApplyBlogIndexChangeUseCase applyBlogIndexChange;
    private final RetryingMessageRecoverer recoverer;

    public BlogMessageListener(
        ApplyBlogIndexChangeUseCase applyBlogIndexChange, RetryingMessageRecoverer recoverer) {
        this.applyBlogIndexChange = applyBlogIndexChange;
        this.recoverer = recoverer;
    }

    @RabbitListener(
        autoStartup = "#{!${megalith.search.maintenance.enabled:false}}",
        queues = Const.ES_QUEUE,
        concurrency = "10",
        messageConverter = "jsonMessageConverter",
        executor = "mqExecutor")
    public void handler(BlogChangedMessage message, Channel channel, Message msg) {
        try {
            applyBlogIndexChange.apply(toCommand(message));
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(msg, channel, failure);
        }
    }

    private static BlogIndexChange toCommand(BlogChangedMessage message) {
        BlogOperateEnum operation = BlogOperateEnum.of(message.operation());
        BlogIndexChange.Operation indexOperation =
            switch (operation) {
                case CREATE -> BlogIndexChange.Operation.CREATE;
                case UPDATE -> BlogIndexChange.Operation.UPDATE;
                case REMOVE -> BlogIndexChange.Operation.REMOVE;
            };
        BlogSnapshot blog = message.blogSnapshot();
        return new BlogIndexChange(
            indexOperation,
            message.revision(),
            new BlogIndexEntry(
                blog.id(),
                blog.userId(),
                blog.status(),
                blog.readCount(),
                blog.title(),
                blog.description(),
                blog.content(),
                blog.created(),
                blog.updated()));
    }
}
