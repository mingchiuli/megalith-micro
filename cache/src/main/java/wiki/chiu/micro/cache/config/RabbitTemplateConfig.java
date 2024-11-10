package wiki.chiu.micro.cache.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * @author mingchiuli
 * @since 2022-12-23 12:32 pm
 */
@AutoConfiguration
@ConditionalOnClass(ConnectionFactory.class)
public class RabbitTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitTemplateConfig.class);

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private Integer port;

    @Bean("cacheRabbitTemplate")
    RabbitTemplate cacheRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //设置抵达broker服务器的回掉
        //当前消息的唯一关联数据、服务器对消息是否成功收到、失败的原因
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) ->
                log.debug("message come to mq or not {}, {}, {}", correlationData, ack, cause));

        //设置抵达消息队列的确认回调
        //只要消息没有投递给指定的队列，就触发这个失败回调
        rabbitTemplate.setReturnsCallback(returned -> log.info("message not come to queue {}", returned));

        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        var retryPolicy = new CircuitBreakerRetryPolicy(
                new SimpleRetryPolicy(10)
        );
        retryPolicy.setOpenTimeout(5000L);
        retryPolicy.setResetTimeout(10000L);
        RetryTemplate retryTemplate = RetryTemplate
                .builder()
                .customPolicy(retryPolicy)
                .build();

        rabbitTemplate.setRetryTemplate(retryTemplate);

        return rabbitTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setAddresses(host);
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        cachingConnectionFactory.setPort(port);
        cachingConnectionFactory.setVirtualHost("/");
        return cachingConnectionFactory;
    }
}
