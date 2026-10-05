package wiki.chiu.micro.common.outbox.application.port.out;

/**
 * Serializes an outbox event to the payload that is stored and later published.
 */
public interface OutboxEventSerializer {

    String serialize(Object event);
}
