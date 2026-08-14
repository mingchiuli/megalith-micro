package wiki.chiu.micro.search.mq;

import com.rabbitmq.client.Channel;
import java.util.List;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.search.mq.handler.BlogIndexSupport;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class BlogMessageListener {

  private final List<BlogIndexSupport> elasticsearchHandlers;
  private final RetryingMessageRecoverer recoverer;

  public BlogMessageListener(
      List<BlogIndexSupport> elasticsearchHandlers, RetryingMessageRecoverer recoverer) {
    this.elasticsearchHandlers = elasticsearchHandlers;
    this.recoverer = recoverer;
  }

  @RabbitListener(
      queues = Const.ES_QUEUE,
      concurrency = "10",
      messageConverter = "jsonMessageConverter",
      executor = "mqExecutor")
  public void handler(BlogChangedMessage message, Channel channel, Message msg) {
    try {
      BlogOperateEnum operation = BlogOperateEnum.of(message.operation());
      BlogIndexSupport handler =
          elasticsearchHandlers.stream()
              .filter(candidate -> candidate.supports(operation))
              .findFirst()
              .orElseThrow(
                  () -> new IllegalArgumentException("Unsupported operation: " + operation));
      handler.process(message);
      channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception failure) {
      recoverer.recover(msg, channel, failure);
    }
  }
}
