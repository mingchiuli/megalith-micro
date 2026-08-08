package wiki.chiu.micro.blog.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import wiki.chiu.micro.blog.event.BlogOperateEvent;
import wiki.chiu.micro.common.lang.BlogOperateMessage;

class BlogOperateEventListenerTest {

  @Test
  void failedAfterCommitPublishIsCountedWithoutEscaping() {
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    BlogOperateEventListener listener = new BlogOperateEventListener(rabbitTemplate, registry);
    doThrow(new IllegalStateException("rabbit unavailable"))
        .when(rabbitTemplate)
        .convertAndSend(anyString(), anyString(), any(BlogOperateMessage.class));

    assertDoesNotThrow(() -> listener.process(new BlogOperateEvent(new BlogOperateMessage(7L, 1))));

    assertEquals(
        1.0,
        registry.counter("megalith.event.publish.failures", "event", "blog-operation").count());
  }
}
