package wiki.chiu.micro.exhibit.config;

import org.springframework.amqp.core.Declarables;
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
    Declarables blogStateRetryTopology() {
        return RetryTopology.forQueue(Const.EXHIBIT_STATE_QUEUE);
    }

    @Bean
    RetryingMessageRecoverer blogStateMessageRecoverer(
        @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
        return new RetryingMessageRecoverer(rabbitTemplate, Const.EXHIBIT_STATE_QUEUE);
    }
}
