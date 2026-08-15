package wiki.chiu.micro.auth.consumer;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import wiki.chiu.micro.auth.cache.AuthCacheKeys;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

class UserRedisCacheEvictMessageListenerTest {

  @Test
  void evictsThroughTheSharedCacheFrameworkBeforeAcknowledging() throws IOException {
    CacheEvictHandler cacheEvictions = mock(CacheEvictHandler.class);
    AuthCacheKeys keyResolver = mock(AuthCacheKeys.class);
    RetryingMessageRecoverer recoverer = mock(RetryingMessageRecoverer.class);
    Channel channel = mock(Channel.class);
    UserRedisCacheEvictMessageListener listener =
        new UserRedisCacheEvictMessageListener(cacheEvictions, keyResolver, recoverer);
    AuthCacheEvictMessage event =
        new AuthCacheEvictMessage("event-1", List.of(7L), List.of(), List.of(), false, false);
    Set<String> keys = Set.of("authz:user:7");
    Message raw = MessageBuilder.withBody(new byte[0]).setDeliveryTag(23L).build();
    when(keyResolver.from(event)).thenReturn(keys);

    listener.handler(event, channel, raw);

    InOrder ordered = inOrder(cacheEvictions, channel);
    ordered.verify(cacheEvictions).evictCache(new HashSet<>(keys));
    ordered.verify(channel).basicAck(23L, false);
  }
}
