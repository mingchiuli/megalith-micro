package wiki.chiu.micro.auth.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;

class UserLocalCacheEvictMessageListenerTest {

  @Test
  void consumesTheExplicitLocalEvictionQueue() throws NoSuchMethodException {
    var handler =
        UserLocalCacheEvictMessageListener.class.getMethod(
            "handler", AuthCacheEvictMessage.class, Channel.class, Message.class);
    RabbitListener listener = handler.getAnnotation(RabbitListener.class);

    assertThat(listener.queues()).containsExactly("#{authLocalEvictQueue.name}");
    assertThat(listener.bindings()).isEmpty();
  }
}
