package org.chiu.micro.exhibit.cache.cache.remote;

import com.rabbitmq.client.Channel;
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
public class BlogRedisCacheEvictMessageListener {

    private final List<BlogCacheEvictHandler> blogCacheEvictHandlers;

    public static final String CACHE_QUEUE = "blog.change.queue.cache";

    public BlogRedisCacheEvictMessageListener(List<BlogCacheEvictHandler> blogCacheEvictHandlers) {
        this.blogCacheEvictHandlers = blogCacheEvictHandlers;
    }

    @RabbitListener(queues = CACHE_QUEUE,
            concurrency = "10",
            messageConverter = "jsonMessageConverter",
            executor = "mqExecutor")
    public void handler(BlogOperateMessage message, Channel channel, Message msg) {
        for (BlogCacheEvictHandler handler : blogCacheEvictHandlers) {
            if (handler.supports(message.typeEnum())) {
                handler.handle(message, channel, msg);
                break;
            }
        }
    }
}
