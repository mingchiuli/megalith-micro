package wiki.chiu.micro.blog.listener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import wiki.chiu.micro.blog.event.BlogOperateEvent;
import wiki.chiu.micro.common.lang.BlogOperateMessage;
import wiki.chiu.micro.common.lang.Const;

@Component
public class BlogOperateEventListener {

  private static final Logger log = LoggerFactory.getLogger(BlogOperateEventListener.class);

  private final RabbitTemplate rabbitTemplate;
  private final Counter publishFailures;

  public BlogOperateEventListener(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry) {
    this.rabbitTemplate = rabbitTemplate;
    this.publishFailures =
        Counter.builder("megalith.event.publish.failures")
            .tag("event", "blog-operation")
            .register(meterRegistry);
  }

  @Async("commonExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void process(BlogOperateEvent event) {
    try {
      BlogOperateMessage messageBody = event.message();
      rabbitTemplate.convertAndSend(Const.BLOG_CHANGE_FANOUT_EXCHANGE, "", messageBody);
    } catch (RuntimeException exception) {
      publishFailures.increment();
      log.error("Failed to publish blog operation after transaction commit", exception);
    }
  }
}
