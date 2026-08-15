package wiki.chiu.micro.auth.consumer;

import com.rabbitmq.client.Channel;
import java.util.HashSet;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.cache.AuthCacheKeys;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class UserRedisCacheEvictMessageListener {

  private final CacheEvictHandler cacheEvictHandler;

  private final AuthCacheKeys authCacheKeys;
  private final RetryingMessageRecoverer recoverer;

  public UserRedisCacheEvictMessageListener(
      CacheEvictHandler cacheEvictHandler,
      AuthCacheKeys authCacheKeys,
      RetryingMessageRecoverer recoverer) {
    this.cacheEvictHandler = cacheEvictHandler;
    this.authCacheKeys = authCacheKeys;
    this.recoverer = recoverer;
  }

  @RabbitListener(
      queues = Const.USER_QUEUE,
      concurrency = "10",
      messageConverter = "jsonMessageConverter",
      executor = "mqExecutor")
  public void handler(AuthCacheEvictMessage message, Channel channel, Message msg) {
    try {
      cacheEvictHandler.evictCache(new HashSet<>(authCacheKeys.from(message)));

      channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
      recoverer.recover(msg, channel, e);
    }
  }
}
