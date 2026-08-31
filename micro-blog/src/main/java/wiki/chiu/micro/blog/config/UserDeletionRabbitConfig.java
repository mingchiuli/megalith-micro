package wiki.chiu.micro.blog.config;

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
public class UserDeletionRabbitConfig {

    @Bean("userDeletedExchange")
    FanoutExchange userDeletedExchange() {
        return new FanoutExchange(Const.USER_DELETED_FANOUT_EXCHANGE, true, false);
    }

    @Bean("userDeletedBlogQueue")
    Queue userDeletedBlogQueue() {
        return new Queue(Const.USER_DELETED_QUEUE, true, false, false);
    }

    @Bean("userDeletedBlogBinding")
    Binding userDeletedBlogBinding(
        @Qualifier("userDeletedBlogQueue") Queue queue,
        @Qualifier("userDeletedExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    Declarables userDeletionRetryTopology() {
        return RetryTopology.forQueue(Const.USER_DELETED_QUEUE);
    }

    @Bean
    RetryingMessageRecoverer userDeletionMessageRecoverer(
        @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
        return new RetryingMessageRecoverer(rabbitTemplate, Const.USER_DELETED_QUEUE);
    }
}
