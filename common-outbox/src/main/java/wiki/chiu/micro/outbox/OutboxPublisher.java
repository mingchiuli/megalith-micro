package wiki.chiu.micro.outbox;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import wiki.chiu.micro.outbox.config.OutboxProperties;
import wiki.chiu.micro.scheduling.RedisTaskLock;

public class OutboxPublisher {

  private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

  private final OutboxStore store;
  private final RabbitTemplate rabbitTemplate;
  private final RedisTaskLock taskLock;
  private final OutboxProperties properties;
  private final ExecutorService publisherExecutor;
  private final Counter published;
  private final Counter failures;
  private final Counter lockMisses;

  public OutboxPublisher(
      OutboxStore store,
      RabbitTemplate rabbitTemplate,
      RedisTaskLock taskLock,
      OutboxProperties properties,
      MeterRegistry meterRegistry) {
    this.store = store;
    this.rabbitTemplate = rabbitTemplate;
    this.taskLock = taskLock;
    this.properties = properties;
    this.publisherExecutor =
        Executors.newFixedThreadPool(
            properties.getPublisherConcurrency(),
            Thread.ofVirtual()
                .name("outbox-" + properties.getProducer().name().toLowerCase() + "-", 0)
                .factory());
    this.published = counter(meterRegistry, "megalith.outbox.published");
    this.failures = counter(meterRegistry, "megalith.outbox.publish.failures");
    this.lockMisses = counter(meterRegistry, "megalith.outbox.lock.misses");
    Gauge.builder(
            "megalith.outbox.pending",
            store,
            target -> target.pendingCount(properties.getProducer()))
        .tag("producer", properties.getProducer().name())
        .register(meterRegistry);
  }

  @Scheduled(fixedDelayString = "${megalith.outbox.poll-interval:200}")
  public void publishReady() {
    try {
      boolean acquired =
          taskLock.tryRun(OutboxLockNames.publisher(properties), this::publishReadyUnderLock);
      if (!acquired) {
        lockMisses.increment();
      }
    } catch (RuntimeException e) {
      failures.increment();
      log.error("Outbox publishing cycle failed for {}", properties.getProducer(), e);
    }
  }

  private void publishReadyUnderLock() {
    List<OutboxRecord> records =
        store.findReady(properties.getProducer(), properties.getBatchSize());
    CompletableFuture.allOf(
            records.stream()
                .map(
                    record ->
                        CompletableFuture.runAsync(() -> publishOne(record), publisherExecutor))
                .toArray(CompletableFuture[]::new))
        .join();
  }

  private void publishOne(OutboxRecord record) {
    try {
      MessageProperties messageProperties = new MessageProperties();
      messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
      messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
      messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
      messageProperties.setMessageId(record.eventId());
      messageProperties.setHeader("eventType", record.eventType());
      Message message =
          new Message(record.payload().getBytes(StandardCharsets.UTF_8), messageProperties);
      CorrelationData correlationData = new CorrelationData(record.eventId());

      rabbitTemplate.send(properties.getExchange(), "", message, correlationData);
      CorrelationData.Confirm confirm =
          correlationData
              .getFuture()
              .get(properties.getConfirmTimeoutMillis(), TimeUnit.MILLISECONDS);
      if (!confirm.ack()) {
        throw new IllegalStateException("broker nack: " + confirm.reason());
      }
      if (correlationData.getReturned() != null) {
        throw new IllegalStateException(
            "unroutable message: " + correlationData.getReturned().getReplyText());
      }

      store.delete(record.id(), record.producer());
      published.increment();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      reschedule(record, e);
    } catch (Exception e) {
      reschedule(record, e);
    }
  }

  private void reschedule(OutboxRecord record, Exception failure) {
    failures.increment();
    int nextAttempt = record.attempts() + 1;
    long baseSeconds =
        switch (nextAttempt) {
          case 1 -> 1;
          case 2 -> 5;
          case 3 -> 30;
          case 4 -> 120;
          default -> 300;
        };
    double jitter = ThreadLocalRandom.current().nextDouble(0.8, 1.2);
    store.reschedule(
        record.id(),
        record.producer(),
        LocalDateTime.now().plusNanos((long) (baseSeconds * jitter * 1_000_000_000L)),
        failure.getMessage());
    if (nextAttempt >= 20) {
      log.error(
          "Outbox event {} has failed {} times and will continue retrying",
          record.eventId(),
          nextAttempt,
          failure);
    } else {
      log.warn("Outbox event {} publish failed", record.eventId(), failure);
    }
  }

  private Counter counter(MeterRegistry registry, String name) {
    return Counter.builder(name)
        .tag("producer", properties.getProducer().name())
        .register(registry);
  }

  @PreDestroy
  void close() {
    publisherExecutor.close();
  }
}
