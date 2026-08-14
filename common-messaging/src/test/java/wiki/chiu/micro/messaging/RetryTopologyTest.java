package wiki.chiu.micro.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

class RetryTopologyTest {

  @Test
  void declaresBoundedDelayQueuesAndExpiringDeadLetterQueue() {
    String mainQueue = "events.main";
    Map<String, Queue> queues =
        RetryTopology.forQueue(mainQueue).getDeclarablesByType(Queue.class).stream()
            .collect(Collectors.toMap(Queue::getName, Function.identity()));

    assertRetryQueue(queues.get("events.main.retry.1"), mainQueue, 5_000);
    assertRetryQueue(queues.get("events.main.retry.2"), mainQueue, 30_000);
    assertRetryQueue(queues.get("events.main.retry.3"), mainQueue, 300_000);
    assertEquals(
        14 * 24 * 60 * 60 * 1000,
        queues.get("events.main.dlq").getArguments().get("x-message-ttl"));
  }

  private void assertRetryQueue(Queue queue, String mainQueue, int delayMillis) {
    assertEquals(delayMillis, queue.getArguments().get("x-message-ttl"));
    assertEquals("", queue.getArguments().get("x-dead-letter-exchange"));
    assertEquals(mainQueue, queue.getArguments().get("x-dead-letter-routing-key"));
  }
}
