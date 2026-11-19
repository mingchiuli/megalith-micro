package wiki.chiu.micro.search.adapter.in.messaging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.search.application.port.in.ApplyBlogIndexChangeUseCase;

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
                BlogMessageConvertor.toCommand(message));
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(msg, channel, failure);
        }
    }
}
