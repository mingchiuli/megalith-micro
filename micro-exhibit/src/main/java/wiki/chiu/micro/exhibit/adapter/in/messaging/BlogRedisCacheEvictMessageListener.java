package wiki.chiu.micro.exhibit.adapter.in.messaging;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.exhibit.application.port.in.BlogChangeEviction;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class BlogRedisCacheEvictMessageListener {

    private final BlogChangeEviction blogChangeEviction;
    private final RetryingMessageRecoverer recoverer;

    public BlogRedisCacheEvictMessageListener(
        BlogChangeEviction blogChangeEviction, RetryingMessageRecoverer recoverer) {
        this.blogChangeEviction = blogChangeEviction;
        this.recoverer = recoverer;
    }

    @RabbitListener(
        queues = Const.CACHE_QUEUE,
        concurrency = "10",
        messageConverter = "jsonMessageConverter",
        executor = "mqExecutor")
    public void handler(BlogChangedMessage message, Channel channel, Message msg) {
        try {
            blogChangeEviction.apply(message);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(msg, channel, failure);
        }
    }
}
