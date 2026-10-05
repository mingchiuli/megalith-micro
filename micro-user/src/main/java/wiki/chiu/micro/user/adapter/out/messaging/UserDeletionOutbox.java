package wiki.chiu.micro.user.adapter.out.messaging;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.message.UserDeletedMessage;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

@Component
public class UserDeletionOutbox {

    private final OutboxEvents outboxService;

    public UserDeletionOutbox(OutboxEvents outboxService) {
        this.outboxService = outboxService;
    }

    public void enqueue(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        outboxService.enqueue(
            OutboxProducer.USER,
            "USER_DELETION",
            userIds.getFirst(),
            eventId -> new UserDeletedMessage(eventId, userIds));
    }
}
