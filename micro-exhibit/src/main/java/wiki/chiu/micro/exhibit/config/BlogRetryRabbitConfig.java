package wiki.chiu.micro.exhibit.config;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryTopology;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

@Configuration(proxyBeanMethods = false)
public class BlogRetryRabbitConfig {

    @Bean
    Declarables cacheRetryTopology() {
        return RetryTopology.forQueue(Const.CACHE_QUEUE);
    }

    @Bean
    RetryingMessageRecoverer cacheMessageRecoverer(
        @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
        return new RetryingMessageRecoverer(rabbitTemplate, Const.CACHE_QUEUE);
    }
}
