package wiki.chiu.micro.user.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import wiki.chiu.micro.common.lang.UserAuthMenuOperateMessage;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;

class AuthMenuOperateEventListenerTest {

  @Test
  void failedAfterCommitPublishIsCountedWithoutEscaping() {
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    AuthMenuOperateEventListener listener =
        new AuthMenuOperateEventListener(rabbitTemplate, registry);
    doThrow(new IllegalStateException("rabbit unavailable"))
        .when(rabbitTemplate)
        .convertAndSend(anyString(), anyString(), any(UserAuthMenuOperateMessage.class));

    assertDoesNotThrow(
        () ->
            listener.process(
                new AuthMenuOperateEvent(new AuthMenuIndexMessage(List.of("admin"), 1))));

    assertEquals(
        1.0,
        registry
            .counter("megalith.event.publish.failures", "event", "auth-menu-operation")
            .count());
  }
}
