package org.chiu.micro.search.mq;

import org.chiu.micro.search.constant.BlogOperateMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.chiu.micro.search.mq.handler.BlogIndexSupport;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
@RequiredArgsConstructor
public class BlogMessageListener {

    private final List<BlogIndexSupport> elasticsearchHandlers;

    private static final String ES_QUEUE = "blog.change.queue.es";

    @RabbitListener(queues = ES_QUEUE,
                    concurrency = "10",
                    messageConverter = "jsonMessageConverter",
                    executor = "mqExecutor")
    public void handler(BlogOperateMessage message, Channel channel, Message msg) {
        for (BlogIndexSupport handler : elasticsearchHandlers) {
            if (handler.supports(message.getTypeEnum())) {
                handler.handle(message, channel, msg);
                break;
            }
        }
    }
}
