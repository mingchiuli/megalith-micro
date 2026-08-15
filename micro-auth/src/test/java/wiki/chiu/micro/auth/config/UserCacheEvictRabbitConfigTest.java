package wiki.chiu.micro.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.core.task.SyncTaskExecutor;
import wiki.chiu.micro.auth.cache.AuthCacheKeys;
import wiki.chiu.micro.auth.consumer.UserLocalCacheEvictMessageListener;
import wiki.chiu.micro.common.lang.Const;

class UserCacheEvictRabbitConfigTest {

  private final UserCacheEvictRabbitConfig config = new UserCacheEvictRabbitConfig();

  @Test
  void localQueueIsNamedExclusiveAndAutoDeleted() {
    Queue queue = config.authLocalEvictQueue();

    assertThat(queue.getName()).startsWith("auth.cache.local.");
    assertThat(queue.isDurable()).isFalse();
    assertThat(queue.isExclusive()).isTrue();
    assertThat(queue.isAutoDelete()).isTrue();
  }

  @Test
  void localQueueIsBoundToTheAuthorizationFanoutExchange() {
    Queue queue = config.authLocalEvictQueue();
    FanoutExchange exchange = config.authChangeExchange();

    Binding binding = config.authLocalEvictBinding(queue, exchange);

    assertThat(binding.getDestination()).isEqualTo(queue.getName());
    assertThat(binding.getExchange()).isEqualTo(Const.USER_CHANGE_FANOUT_EXCHANGE);
  }

  @Test
  void localListenerContainerUsesTheConcreteQueueNameWithoutSpel() {
    Queue queue = config.authLocalEvictQueue();
    @SuppressWarnings("unchecked")
    Cache<String, Object> localCache = mock(Cache.class);
    var listener = new UserLocalCacheEvictMessageListener(localCache, mock(AuthCacheKeys.class));
    MessageListenerAdapter listenerAdapter = config.authLocalEvictMessageListenerAdapter(listener);

    var container =
        config.authLocalEvictMessageListenerContainer(
            mock(ConnectionFactory.class), listenerAdapter, queue, new SyncTaskExecutor());

    assertThat(container.getQueueNames()).containsExactly(queue.getName());
    assertThat(container.getMessageListener()).isSameAs(listenerAdapter);
  }
}
