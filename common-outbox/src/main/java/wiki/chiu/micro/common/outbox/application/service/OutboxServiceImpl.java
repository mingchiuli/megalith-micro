package wiki.chiu.micro.common.outbox.application.service;

import java.util.UUID;
import java.util.function.Function;

import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;
import wiki.chiu.micro.common.outbox.application.port.out.OutboxEventSerializer;
import wiki.chiu.micro.common.outbox.application.port.out.OutboxStore;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

public class OutboxServiceImpl implements OutboxEvents {

    private final OutboxStore store;

    private final OutboxEventSerializer serializer;

    public OutboxServiceImpl(OutboxStore store, OutboxEventSerializer serializer) {
        this.store = store;
        this.serializer = serializer;
    }

    @Override
    public String enqueue(
        OutboxProducer producer, String aggregateType, Object aggregateId, Object event) {
        String eventId = UUID.randomUUID().toString();
        insert(eventId, producer, aggregateType, aggregateId, event);
        return eventId;
    }

    @Override
    public String enqueue(
        OutboxProducer producer,
        String aggregateType,
        Object aggregateId,
        Function<String, Object> eventFactory) {
        String eventId = UUID.randomUUID().toString();
        insert(eventId, producer, aggregateType, aggregateId, eventFactory.apply(eventId));
        return eventId;
    }

    private void insert(
        String eventId,
        OutboxProducer producer,
        String aggregateType,
        Object aggregateId,
        Object event) {
        store.insert(
            eventId,
            producer,
            aggregateType,
            String.valueOf(aggregateId),
            event.getClass().getSimpleName(),
            serializer.serialize(event));
    }
}
