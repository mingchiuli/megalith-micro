package wiki.chiu.micro.blog.listener;

import wiki.chiu.micro.blog.config.BlogChangeRabbitConfig;
import wiki.chiu.micro.blog.constant.BlogOperateMessage;
import wiki.chiu.micro.blog.event.BlogOperateEvent;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class BlogOperateEventListener {

    private final RabbitTemplate rabbitTemplate;

    public final StringRedisTemplate redisTemplate;

    public BlogOperateEventListener(RabbitTemplate rabbitTemplate, StringRedisTemplate redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    @EventListener
    @Async("commonExecutor")
    public void process(BlogOperateEvent event) {
        BlogOperateMessage messageBody = event.getBlogOperateMessage();
        rabbitTemplate.convertAndSend(BlogChangeRabbitConfig.FANOUT_EXCHANGE, "", messageBody);
    }
}
