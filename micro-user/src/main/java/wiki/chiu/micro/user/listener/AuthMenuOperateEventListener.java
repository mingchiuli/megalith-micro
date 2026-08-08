package wiki.chiu.micro.user.listener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.UserAuthMenuOperateMessage;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;

@Component
public class AuthMenuOperateEventListener {

  private static final Logger log = LoggerFactory.getLogger(AuthMenuOperateEventListener.class);

  private final RabbitTemplate rabbitTemplate;
  private final Counter publishFailures;

  public AuthMenuOperateEventListener(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry) {
    this.rabbitTemplate = rabbitTemplate;
    this.publishFailures =
        Counter.builder("megalith.event.publish.failures")
            .tag("event", "auth-menu-operation")
            .register(meterRegistry);
  }

  @Async("commonExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void process(AuthMenuOperateEvent event) {
    try {
      AuthMenuIndexMessage authMenuIndexMessage = event.message();
      List<String> roles = authMenuIndexMessage.roles();
      Integer type = authMenuIndexMessage.type();
      var data = UserAuthMenuOperateMessage.builder().roles(roles).type(type).build();
      rabbitTemplate.convertAndSend(Const.USER_CHANGE_FANOUT_EXCHANGE, "", data);
    } catch (RuntimeException exception) {
      publishFailures.increment();
      log.error("Failed to publish auth/menu operation after transaction commit", exception);
    }
  }
}
