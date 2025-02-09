package wiki.chiu.micro.exhibit.consumer;

import com.rabbitmq.client.Channel;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogOperateMessage;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.exhibit.consumer.cache.handler.BlogCacheEvictHandler;
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

    public BlogRedisCacheEvictMessageListener(List<BlogCacheEvictHandler> blogCacheEvictHandlers) {
        this.blogCacheEvictHandlers = blogCacheEvictHandlers;
    }

    @RabbitListener(queues = Const.CACHE_QUEUE,
            concurrency = "10",
            messageConverter = "jsonMessageConverter",
            executor = "mqExecutor")
    public void handler(BlogOperateMessage message, Channel channel, Message msg) {
        for (BlogCacheEvictHandler handler : blogCacheEvictHandlers) {
            if (handler.supports(BlogOperateEnum.of(message.typeEnumCode()))) {
                handler.handle(message, channel, msg);
                break;
            }
        }
    }
}
