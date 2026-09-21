package wiki.chiu.micro.common.outbox.application.model;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

public record OutboxRecord(
    long id,
    String eventId,
    OutboxProducer producer,
    String aggregateType,
    String aggregateId,
    String eventType,
    String payload,
    int attempts,
    LocalDateTime availableAt) {
}
