package wiki.chiu.micro.user.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

/**
 * @author mingchiuli
 * @create 2022-12-23 12:32 pm
 */
@Configuration(proxyBeanMethods = false)
public class RabbitTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitTemplateConfig.class);
    private final RabbitTemplate rabbitTemplate;

    private final JacksonJsonMessageConverter jsonMessageConverter;

    public RabbitTemplateConfig(RabbitTemplate rabbitTemplate, JacksonJsonMessageConverter jsonMessageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMessageConverter = jsonMessageConverter;
    }

    @PostConstruct
    public void initRabbitTemplate() {
        //设置抵达broker服务器的回掉
        //当前消息的唯一关联数据、服务器对消息是否成功收到、失败的原因
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) ->
                log.debug("message come to mq or not {}, {}, {}", correlationData, ack, cause));

        //设置抵达消息队列的确认回调
        //只要消息没有投递给指定的队列，就触发这个失败回调
        rabbitTemplate.setReturnsCallback(returned -> log.info("message not come to queue {}", returned));

        rabbitTemplate.setMessageConverter(jsonMessageConverter);

        rabbitTemplate.setRetryTemplate(new RetryTemplate(RetryPolicy.withDefaults()));
    }
}
