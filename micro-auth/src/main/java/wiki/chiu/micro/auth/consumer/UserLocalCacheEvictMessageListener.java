package wiki.chiu.micro.auth.consumer;

import com.github.benmanes.caffeine.cache.Cache;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.cache.AuthCacheKeys;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;

@Component
public class UserLocalCacheEvictMessageListener {

  private static final Logger log =
      LoggerFactory.getLogger(UserLocalCacheEvictMessageListener.class);

  private final Cache<@NonNull String, Object> localCache;
  private final AuthCacheKeys authCacheKeys;

  public UserLocalCacheEvictMessageListener(
      @Qualifier("caffeineCache") Cache<@NonNull String, Object> localCache,
      AuthCacheKeys authCacheKeys) {
    this.localCache = localCache;
    this.authCacheKeys = authCacheKeys;
  }

  @RabbitListener(
      queues = "#{authLocalEvictQueue.name}",
      messageConverter = "jsonMessageConverter",
      executor = "mqExecutor")
  public void handler(AuthCacheEvictMessage message, Channel channel, Message raw) {
    try {
      localCache.invalidateAll(authCacheKeys.from(message));
      channel.basicAck(raw.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
      log.error("Failed to evict local auth cache", e);
      try {
        channel.basicNack(raw.getMessageProperties().getDeliveryTag(), false, false);
      } catch (IOException ioException) {
        log.error("Failed to nack local cache eviction", ioException);
      }
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public void clearOnStartup() {
    localCache.invalidateAll();
  }
}
