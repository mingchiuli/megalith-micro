package wiki.chiu.micro.common.outbox.application.service;

import wiki.chiu.micro.common.outbox.application.model.OutboxStatus;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxAdministration;
import wiki.chiu.micro.common.outbox.application.port.out.OutboxStore;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

public class OutboxAdministrationServiceImpl implements OutboxAdministration {

    private final OutboxStore store;

    public OutboxAdministrationServiceImpl(OutboxStore store) {
        this.store = store;
    }

    @Override
    public OutboxStatus status(OutboxProducer producer) {
        return store.status(producer);
    }

    @Override
    public boolean manage(OutboxProducer producer, String eventId, String action) {
        return store.manage(eventId, producer, action);
    }
}
