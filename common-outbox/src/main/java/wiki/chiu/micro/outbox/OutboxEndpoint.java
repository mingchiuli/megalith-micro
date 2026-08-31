package wiki.chiu.micro.outbox;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import wiki.chiu.micro.outbox.config.OutboxProperties;
import wiki.chiu.micro.scheduling.RedisTaskLock;

@Endpoint(id = "outbox")
public class OutboxEndpoint {

    private final OutboxStore store;
    private final OutboxProperties properties;
    private final RedisTaskLock taskLock;

    public OutboxEndpoint(OutboxStore store, OutboxProperties properties, RedisTaskLock taskLock) {
        this.store = store;
        this.properties = properties;
        this.taskLock = taskLock;
    }

    @ReadOperation
    public OutboxStatus status() {
        return store.status(properties.getProducer());
    }

    @WriteOperation
    public boolean manage(@Selector String eventId, String action) {
        return taskLock.run(
            OutboxLockNames.publisher(properties),
            () -> store.manage(eventId, properties.getProducer(), action));
    }
}
