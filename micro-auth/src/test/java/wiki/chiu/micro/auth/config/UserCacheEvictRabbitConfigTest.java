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
  void remoteQueueIsDurableAndShared() {
    Queue queue = config.authRemoteEvictQueue();

    assertThat(queue.getName()).isEqualTo(Const.USER_QUEUE);
    assertThat(queue.isDurable()).isTrue();
    assertThat(queue.isExclusive()).isFalse();
    assertThat(queue.isAutoDelete()).isFalse();
  }

  @Test
  void remoteQueueIsBoundToTheAuthorizationFanoutExchange() {
    Queue queue = config.authRemoteEvictQueue();
    FanoutExchange exchange = config.authChangeExchange();

    Binding binding = config.authRemoteEvictBinding(queue, exchange);

    assertThat(binding.getDestination()).isEqualTo(queue.getName());
    assertThat(binding.getExchange()).isEqualTo(Const.USER_CHANGE_FANOUT_EXCHANGE);
  }
}
