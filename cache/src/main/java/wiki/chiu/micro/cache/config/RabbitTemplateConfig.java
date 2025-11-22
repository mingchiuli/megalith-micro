package wiki.chiu.micro.cache.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

/**
 * @author mingchiuli
 * @since 2022-12-23 12:32 pm
 */
@AutoConfiguration
@ConditionalOnClass({ConnectionFactory.class, RabbitAutoConfiguration.class}) // 同时检查两个类
@ConditionalOnBean(ConnectionFactory.class)
@AutoConfigureAfter(RabbitAutoConfiguration.class) // 此时RabbitAutoConfiguration一定存在
public class RabbitTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitTemplateConfig.class);

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

        rabbitTemplate.setMessageConverter(new JacksonJsonMessageConverter());

        rabbitTemplate.setRetryTemplate(new RetryTemplate(RetryPolicy.withDefaults()));
        return rabbitTemplate;
    }
}
