package wiki.chiu.micro.search.adapter.in.messaging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.search.application.port.in.ApplyBlogIndexChangeUseCase;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class BlogMessageListener {

    private final ApplyBlogIndexChangeUseCase applyBlogIndexChange;
    private final RetryingMessageRecoverer recoverer;

    public BlogMessageListener(
        ApplyBlogIndexChangeUseCase applyBlogIndexChange,
        @Qualifier("searchMessageRecoverer") RetryingMessageRecoverer recoverer) {
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
            applyBlogIndexChange.apply(BlogMessageConvertor.toCommand(message));
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(msg, channel, failure);
        }
    }

}
