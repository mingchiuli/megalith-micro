package wiki.chiu.micro.outbox;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;

public class OutboxStore {

  private final OutboxEventRepository repository;

  public OutboxStore(OutboxEventRepository repository) {
    this.repository = repository;
  }

  public void insert(
      String eventId,
      OutboxProducer producer,
      String aggregateType,
      String aggregateId,
      String eventType,
      String payload) {
    LocalDateTime now = LocalDateTime.now();
    OutboxEventEntity entity = new OutboxEventEntity();
    entity.setEventId(eventId);
    entity.setProducer(producer);
    entity.setAggregateType(aggregateType);
    entity.setAggregateId(aggregateId);
    entity.setEventType(eventType);
    entity.setPayload(payload);
    entity.setState("READY");
    entity.setAttempts(0);
    entity.setAvailableAt(now);
    entity.setCreatedAt(now);
    repository.save(entity);
  }

  public List<OutboxRecord> findReady(OutboxProducer producer, int limit) {
    return repository.findReady(producer, LocalDateTime.now(), PageRequest.ofSize(limit)).stream()
        .map(OutboxStore::toRecord)
        .toList();
  }

  public boolean delete(long id, OutboxProducer producer) {
    return repository.deleteByIdAndProducer(id, producer) == 1;
  }

  public void reschedule(
      long id, OutboxProducer producer, LocalDateTime availableAt, String error) {
    repository.reschedule(id, producer, availableAt, truncate(error));
  }

  public long pendingCount(OutboxProducer producer) {
    return repository.countByProducerAndState(producer, "READY");
  }

  public OutboxStatus status(OutboxProducer producer) {
    long ready = repository.countByProducerAndState(producer, "READY");
    long paused = repository.countByProducerAndState(producer, "PAUSED");
    int maxAttempts = repository.maxAttempts(producer);
    return new OutboxStatus(producer, ready, paused, maxAttempts);
  }

  public boolean manage(String eventId, OutboxProducer producer, String action) {
    return switch (action.toLowerCase(java.util.Locale.ROOT)) {
      case "pause" -> repository.pause(eventId, producer) == 1;
      case "resume", "retry" -> repository.resume(eventId, producer, LocalDateTime.now()) == 1;
      case "discard" -> repository.discard(eventId, producer) == 1;
      default -> throw new IllegalArgumentException("Unsupported outbox action: " + action);
    };
  }

  private static OutboxRecord toRecord(OutboxEventEntity entity) {
    return new OutboxRecord(
        entity.getId(),
        entity.getEventId(),
        entity.getProducer(),
        entity.getAggregateType(),
        entity.getAggregateId(),
        entity.getEventType(),
        entity.getPayload(),
        entity.getAttempts(),
        entity.getAvailableAt());
  }

  private static String truncate(String error) {
    if (error == null) {
      return "unknown publish failure";
    }
    return error.length() <= 2000 ? error : error.substring(0, 2000);
  }
}
