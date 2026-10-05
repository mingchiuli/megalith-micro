package wiki.chiu.micro.user.adapter.out.messaging;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.message.AuthCacheEvictMessage;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

@Component
public class AuthCacheEvictionOutbox {

    private final OutboxEvents outboxService;

    public AuthCacheEvictionOutbox(OutboxEvents outboxService) {
        this.outboxService = outboxService;
    }

    public void enqueue(
        List<Long> userIds,
        List<Long> roleIds,
        List<String> roleCodes,
        boolean evictMenus,
        boolean evictRoutes) {
        Object aggregateId =
            !userIds.isEmpty()
                ? userIds.getFirst()
                : !roleIds.isEmpty() ? roleIds.getFirst() : "GLOBAL";
        outboxService.enqueue(
            OutboxProducer.USER,
            "AUTHORIZATION",
            aggregateId,
            eventId ->
                new AuthCacheEvictMessage(
                    eventId, userIds, roleIds, roleCodes, evictMenus, evictRoutes));
    }
}
