package org.chiu.micro.exhibit.cache.mq;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.chiu.micro.exhibit.cache.handler.BlogCacheEvictHandler;
import org.chiu.micro.exhibit.constant.BlogOperateMessage;
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
public class BlogRedisCacheEvictMessageListener {

    private final List<BlogCacheEvictHandler> blogCacheEvictHandlers;

    public static final String CACHE_QUEUE = "blog.change.queue.cache";

    @RabbitListener(queues = CACHE_QUEUE,
                    concurrency = "10",
                    messageConverter = "jsonMessageConverter",
                    executor = "mqExecutor")
    public void handler(BlogOperateMessage message, Channel channel, Message msg) {
        for (BlogCacheEvictHandler handler : blogCacheEvictHandlers) {
            if (handler.supports(message.getTypeEnum())) {
                handler.handle(message, channel, msg);
                break;
            }
        }
    }
}
