package wiki.chiu.micro.blog.listener;

import wiki.chiu.micro.common.lang.BlogOperateMessage;
import wiki.chiu.micro.blog.event.BlogOperateEvent;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.Const;


@Component
public class BlogOperateEventListener {

    private final RabbitTemplate rabbitTemplate;

    public final StringRedisTemplate redisTemplate;

    public BlogOperateEventListener(RabbitTemplate rabbitTemplate, StringRedisTemplate redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    @EventListener
    public void process(BlogOperateEvent event) {
        BlogOperateMessage messageBody = event.getBlogOperateMessage();
        rabbitTemplate.convertAndSend(Const.BLOG_CHANGE_FANOUT_EXCHANGE, "", messageBody);
    }
}
