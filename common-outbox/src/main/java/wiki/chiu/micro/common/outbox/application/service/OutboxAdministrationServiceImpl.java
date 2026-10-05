package wiki.chiu.micro.common.outbox.application.service;

import wiki.chiu.micro.common.outbox.application.model.OutboxStatus;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxAdministration;
import wiki.chiu.micro.common.outbox.application.port.out.OutboxStore;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

public class OutboxAdministrationServiceImpl implements OutboxAdministration {

    private final OutboxStore store;

    private final OutboxProducer producer;

    public OutboxAdministrationServiceImpl(OutboxStore store, OutboxProducer producer) {
        this.store = store;
        this.producer = producer;
    }

    @Override
    public OutboxStatus status() {
        return store.status(producer);
    }

    @Override
    public boolean manage(String eventId, String action) {
        return store.manage(eventId, producer, action);
    }
}
