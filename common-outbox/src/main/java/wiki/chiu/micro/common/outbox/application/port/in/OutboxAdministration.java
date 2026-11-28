package wiki.chiu.micro.common.outbox.application.port.in;

import wiki.chiu.micro.common.outbox.application.model.OutboxStatus;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

/**
 * Read and manage operations for the outbox of one producer.
 */
public interface OutboxAdministration {

    OutboxStatus status(OutboxProducer producer);

    boolean manage(OutboxProducer producer, String eventId, String action);
}
