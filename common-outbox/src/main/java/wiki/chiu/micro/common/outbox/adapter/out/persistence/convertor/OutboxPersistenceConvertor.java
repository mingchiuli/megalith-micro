package wiki.chiu.micro.common.outbox.adapter.out.persistence.convertor;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.outbox.adapter.out.persistence.entity.OutboxEventEntity;
import wiki.chiu.micro.common.outbox.application.model.OutboxRecord;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

public final class OutboxPersistenceConvertor {

    private OutboxPersistenceConvertor() {
    }

    public static OutboxRecord toRecord(OutboxEventEntity entity) {
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

    public static OutboxEventEntity toEntity(
        String eventId, OutboxProducer producer, String aggregateType, String aggregateId,
        String eventType, String payload, LocalDateTime now) {
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
        return entity;
    }
}
