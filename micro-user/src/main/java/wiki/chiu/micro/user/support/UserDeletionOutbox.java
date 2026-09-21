package wiki.chiu.micro.user.support;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.message.UserDeletedMessage;
import wiki.chiu.micro.common.outbox.application.OutboxService;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

@Component
public class UserDeletionOutbox {

    private final OutboxService outboxService;

    public UserDeletionOutbox(OutboxService outboxService) {
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
