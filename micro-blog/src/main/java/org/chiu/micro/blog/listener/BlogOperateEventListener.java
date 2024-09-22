package org.chiu.micro.blog.listener;

import org.chiu.micro.blog.config.BlogChangeRabbitConfig;
import org.chiu.micro.blog.constant.BlogOperateMessage;
import org.chiu.micro.blog.event.BlogOperateEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class BlogOperateEventListener {

    private final RabbitTemplate rabbitTemplate;

    public BlogOperateEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    @Async("commonExecutor")
    public void process(BlogOperateEvent event) {
        BlogOperateMessage messageBody = event.getBlogOperateMessage();

        rabbitTemplate.convertAndSend(BlogChangeRabbitConfig.FANOUT_EXCHANGE, "", messageBody);
    }
}
