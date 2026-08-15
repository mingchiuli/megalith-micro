package wiki.chiu.micro.auth.config;

import java.util.UUID;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import wiki.chiu.micro.auth.consumer.UserLocalCacheEvictMessageListener;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryTopology;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

@Configuration(proxyBeanMethods = false)
public class UserCacheEvictRabbitConfig {

  @Bean("authChangeExchange")
  FanoutExchange authChangeExchange() {
    return new FanoutExchange(Const.USER_CHANGE_FANOUT_EXCHANGE, true, false);
  }

  @Bean("authRemoteEvictQueue")
  Queue authRemoteEvictQueue() {
    return new Queue(Const.USER_QUEUE, true, false, false);
  }

  @Bean("authRemoteEvictBinding")
  Binding authRemoteEvictBinding(
      @Qualifier("authRemoteEvictQueue") Queue queue,
      @Qualifier("authChangeExchange") FanoutExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange);
  }

  @Bean("authLocalEvictQueue")
  Queue authLocalEvictQueue() {
    return new Queue("auth.cache.local." + UUID.randomUUID(), false, true, true);
  }

  @Bean("authLocalEvictBinding")
  Binding authLocalEvictBinding(
      @Qualifier("authLocalEvictQueue") Queue queue,
      @Qualifier("authChangeExchange") FanoutExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange);
  }

  @Bean("authLocalEvictMessageListenerAdapter")
  MessageListenerAdapter authLocalEvictMessageListenerAdapter(
      UserLocalCacheEvictMessageListener listener) {
    return new MessageListenerAdapter(listener);
  }

  @Bean("authLocalEvictMessageListenerContainer")
  SimpleMessageListenerContainer authLocalEvictMessageListenerContainer(
      ConnectionFactory connectionFactory,
      @Qualifier("authLocalEvictMessageListenerAdapter") MessageListenerAdapter listenerAdapter,
      @Qualifier("authLocalEvictQueue") Queue queue,
      @Qualifier("mqExecutor") TaskExecutor taskExecutor) {
    var container = new SimpleMessageListenerContainer();
    listenerAdapter.containerAckMode(AcknowledgeMode.MANUAL);
    listenerAdapter.setMessageConverter(new JacksonJsonMessageConverter());
    container.setConcurrency("5");
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(queue.getName());
    container.setMessageListener(listenerAdapter);
    container.setTaskExecutor(taskExecutor);
    return container;
  }

  @Bean
  Declarables authRetryTopology() {
    return RetryTopology.forQueue(Const.USER_QUEUE);
  }

  @Bean
  RetryingMessageRecoverer authMessageRecoverer(
      @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
    return new RetryingMessageRecoverer(rabbitTemplate, Const.USER_QUEUE);
  }
}
