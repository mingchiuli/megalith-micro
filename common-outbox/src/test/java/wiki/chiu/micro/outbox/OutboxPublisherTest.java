package wiki.chiu.micro.outbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import wiki.chiu.micro.outbox.config.OutboxProperties;
import wiki.chiu.micro.scheduling.RedisTaskLock;
import wiki.chiu.micro.scheduling.TaskLockProperties;

class OutboxPublisherTest {

  private final OutboxStore store = mock(OutboxStore.class);
  private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
  private final RedissonClient redisson = mock(RedissonClient.class);
  private OutboxPublisher publisher;

  @BeforeEach
  void setUp() {
    OutboxProperties properties = new OutboxProperties();
    properties.setProducer(OutboxProducer.BLOG);
    properties.setExchange("blog.exchange");
    properties.setPublisherConcurrency(1);
    properties.setConfirmTimeoutMillis(100);
    org.redisson.api.RLock lock = mock(org.redisson.api.RLock.class);
    when(redisson.getLock("megalith:default:task:outbox:publisher:BLOG")).thenReturn(lock);
    when(lock.tryLock()).thenReturn(true);
    when(lock.isHeldByCurrentThread()).thenReturn(true);
    RedisTaskLock taskLock = new RedisTaskLock(redisson, new TaskLockProperties());
    publisher =
        new OutboxPublisher(store, rabbitTemplate, taskLock, properties, new SimpleMeterRegistry());
  }

  @AfterEach
  void tearDown() {
    publisher.close();
  }

  @Test
  void confirmedMessageIsPhysicallyDeleted() {
    OutboxRecord record = record();
    when(store.findReady(OutboxProducer.BLOG, 50)).thenReturn(List.of(record));
    completeConfirm(true, null);

    publisher.publishReady();

    verify(store).delete(record.id(), OutboxProducer.BLOG);
    verify(store, never())
        .reschedule(
            eq(record.id()), eq(OutboxProducer.BLOG), any(LocalDateTime.class), any(String.class));
  }

  @Test
  void brokerNackSchedulesAutomaticRetryWithoutDeleting() {
    OutboxRecord record = record();
    when(store.findReady(OutboxProducer.BLOG, 50)).thenReturn(List.of(record));
    completeConfirm(false, "broker unavailable");

    publisher.publishReady();

    verify(store, never()).delete(record.id(), OutboxProducer.BLOG);
    verify(store)
        .reschedule(
            eq(record.id()),
            eq(OutboxProducer.BLOG),
            any(LocalDateTime.class),
            eq("broker nack: broker unavailable"));
  }

  private void completeConfirm(boolean ack, String reason) {
    doAnswer(
            invocation -> {
              CorrelationData correlation = invocation.getArgument(3);
              correlation.getFuture().complete(new CorrelationData.Confirm(ack, reason));
              return null;
            })
        .when(rabbitTemplate)
        .send(eq("blog.exchange"), eq(""), any(Message.class), any(CorrelationData.class));
  }

  private OutboxRecord record() {
    return new OutboxRecord(
        1L,
        "event-1",
        OutboxProducer.BLOG,
        "BLOG",
        "42",
        "BlogChangedMessage",
        "{}",
        0,
        LocalDateTime.now());
  }
}
