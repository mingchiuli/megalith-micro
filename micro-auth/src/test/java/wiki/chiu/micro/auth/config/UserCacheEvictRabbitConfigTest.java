package wiki.chiu.micro.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
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
}
