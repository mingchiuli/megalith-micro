package wiki.chiu.micro.user.support;

import java.util.List;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;
import wiki.chiu.micro.outbox.OutboxProducer;
import wiki.chiu.micro.outbox.OutboxService;

@Component
public class AuthCacheEvictionOutbox {

  private final OutboxService outboxService;

  public AuthCacheEvictionOutbox(OutboxService outboxService) {
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
