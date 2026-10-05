package wiki.chiu.micro.common.outbox.adapter.in.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import wiki.chiu.micro.common.outbox.application.OutboxLockNames;
import wiki.chiu.micro.common.outbox.application.model.OutboxStatus;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxAdministration;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;
import wiki.chiu.micro.common.scheduling.RedisTaskLock;

@Endpoint(id = "outbox")
public class OutboxEndpoint {

    private final OutboxAdministration administration;
    private final OutboxProducer producer;
    private final RedisTaskLock taskLock;

    public OutboxEndpoint(
        OutboxAdministration administration, OutboxProducer producer, RedisTaskLock taskLock) {
        this.administration = administration;
        this.producer = producer;
        this.taskLock = taskLock;
    }

    @ReadOperation
    public OutboxStatus status() {
        return administration.status();
    }

    @WriteOperation
    public boolean manage(@Selector String eventId, String action) {
        return taskLock.run(
            OutboxLockNames.publisher(producer.name()), () -> administration.manage(eventId, action));
    }
}
