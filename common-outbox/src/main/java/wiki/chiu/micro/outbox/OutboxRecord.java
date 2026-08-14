package wiki.chiu.micro.outbox;

import java.time.LocalDateTime;

public record OutboxRecord(
    long id,
    String eventId,
    OutboxProducer producer,
    String aggregateType,
    String aggregateId,
    String eventType,
    String payload,
    int attempts,
    LocalDateTime availableAt) {}
