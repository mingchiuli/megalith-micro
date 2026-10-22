package wiki.chiu.micro.search.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.messaging.RetryTopology;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;

@Configuration(proxyBeanMethods = false)
public class BlogRetryRabbitConfig {

    @Bean
    Declarables searchRetryTopology() {
        return RetryTopology.forQueue(Const.ES_QUEUE);
    }

    @Bean
    Declarables adminSearchRetryTopology() {
        Queue queue = new Queue(Const.ADMIN_ES_QUEUE, true, false, false);
        FanoutExchange exchange =
            new FanoutExchange(Const.BLOG_ADMIN_FANOUT_EXCHANGE, true, false);
        Binding binding = BindingBuilder.bind(queue).to(exchange);
        List<Declarable> declarations = new ArrayList<>();
        declarations.add(queue);
        declarations.add(exchange);
        declarations.add(binding);
        declarations.addAll(
            RetryTopology.forQueue(Const.ADMIN_ES_QUEUE).getDeclarables());
        return new Declarables(declarations);
    }

    @Bean
    RetryingMessageRecoverer searchMessageRecoverer(
        @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
        return new RetryingMessageRecoverer(rabbitTemplate, Const.ES_QUEUE);
    }

    @Bean("adminSearchMessageRecoverer")
    RetryingMessageRecoverer adminSearchMessageRecoverer(
        @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
        return new RetryingMessageRecoverer(rabbitTemplate, Const.ADMIN_ES_QUEUE);
    }
}
