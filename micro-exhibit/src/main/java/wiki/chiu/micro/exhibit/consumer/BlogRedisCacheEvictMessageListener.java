package wiki.chiu.micro.exhibit.consumer;

import com.rabbitmq.client.Channel;
import java.util.List;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.exhibit.consumer.cache.handler.BlogCacheEvictHandler;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class BlogRedisCacheEvictMessageListener {

  private final List<BlogCacheEvictHandler> blogCacheEvictHandlers;
  private final RetryingMessageRecoverer recoverer;

  public BlogRedisCacheEvictMessageListener(
      List<BlogCacheEvictHandler> blogCacheEvictHandlers, RetryingMessageRecoverer recoverer) {
    this.blogCacheEvictHandlers = blogCacheEvictHandlers;
    this.recoverer = recoverer;
  }

  @RabbitListener(
      queues = Const.CACHE_QUEUE,
      concurrency = "10",
      messageConverter = "jsonMessageConverter",
      executor = "mqExecutor")
  public void handler(BlogChangedMessage message, Channel channel, Message msg) {
    try {
      BlogOperateEnum operation = BlogOperateEnum.of(message.operation());
      BlogCacheEvictHandler handler =
          blogCacheEvictHandlers.stream()
              .filter(candidate -> candidate.supports(operation))
              .findFirst()
              .orElseThrow(
                  () -> new IllegalArgumentException("Unsupported operation: " + operation));
      handler.process(message);
      channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception failure) {
      recoverer.recover(msg, channel, failure);
    }
  }
}
