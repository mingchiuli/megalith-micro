package wiki.chiu.micro.search.adapter.in.messaging;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.search.application.port.in.ApplyBlogIndexChangeUseCase;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;

@Component
public class BlogAdminMessageListener {

    private final ApplyBlogIndexChangeUseCase applyBlogIndexChange;
    private final RetryingMessageRecoverer recoverer;

    public BlogAdminMessageListener(
        ApplyBlogIndexChangeUseCase applyBlogIndexChange,
        @Qualifier("adminSearchMessageRecoverer") RetryingMessageRecoverer recoverer) {
        this.applyBlogIndexChange = applyBlogIndexChange;
        this.recoverer = recoverer;
    }

    @RabbitListener(
        autoStartup = "#{!${megalith.search.maintenance.enabled:false}}",
        queues = Const.ADMIN_ES_QUEUE,
        concurrency = "4",
        messageConverter = "jsonMessageConverter",
        executor = "mqExecutor")
    public void handler(BlogAdminIndexMessage message, Channel channel, Message msg) {
        try {
            applyBlogIndexChange.applyAdmin(
                new BlogAdminIndexChange(
                    message.operation(),
                    message.blog(),
                    message.draft(),
                    message.revision()));
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(msg, channel, failure);
        }
    }
}
