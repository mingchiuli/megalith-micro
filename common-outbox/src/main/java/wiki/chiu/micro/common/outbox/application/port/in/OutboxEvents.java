package wiki.chiu.micro.common.outbox.application.port.in;

import java.util.function.Function;

import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

/**
 * Enqueues domain events into the transactional outbox.
 */
public interface OutboxEvents {

    String enqueue(OutboxProducer producer, String aggregateType, Object aggregateId, Object event);

    String enqueue(
        OutboxProducer producer,
        String aggregateType,
        Object aggregateId,
        Function<String, Object> eventFactory);
}
