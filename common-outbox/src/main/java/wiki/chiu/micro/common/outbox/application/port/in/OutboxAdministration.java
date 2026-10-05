package wiki.chiu.micro.common.outbox.application.port.in;

import wiki.chiu.micro.common.outbox.application.model.OutboxStatus;

/**
 * Read and manage operations for the outbox of one producer.
 */
public interface OutboxAdministration {

    OutboxStatus status();

    boolean manage(String eventId, String action);
}
