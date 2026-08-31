package wiki.chiu.micro.auth.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
