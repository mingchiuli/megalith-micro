package wiki.chiu.micro.common.outbox.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.outbox.application.model.OutboxRecord;
import wiki.chiu.micro.common.outbox.application.model.OutboxStatus;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

/**
 * Persists outbox events and exposes the state the publisher and the actuator endpoint need.
 */
public interface OutboxStore {

    void insert(
        String eventId,
        OutboxProducer producer,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload);

    List<OutboxRecord> findReady(OutboxProducer producer, int limit);

    boolean delete(long id, OutboxProducer producer);

    void reschedule(long id, OutboxProducer producer, LocalDateTime availableAt, String error);

    long pendingCount(OutboxProducer producer);

    OutboxStatus status(OutboxProducer producer);

    boolean manage(String eventId, OutboxProducer producer, String action);
}
