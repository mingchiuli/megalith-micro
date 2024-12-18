package wiki.chiu.micro.search.mq;

import com.rabbitmq.client.Channel;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.search.constant.BlogOperateMessage;
import wiki.chiu.micro.search.mq.handler.BlogIndexSupport;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class BlogMessageListener {

    private final List<BlogIndexSupport> elasticsearchHandlers;

    public BlogMessageListener(List<BlogIndexSupport> elasticsearchHandlers) {
        this.elasticsearchHandlers = elasticsearchHandlers;
    }

    @RabbitListener(queues = Const.ES_QUEUE,
            concurrency = "10",
            messageConverter = "jsonMessageConverter",
            executor = "mqExecutor")
    public void handler(BlogOperateMessage message, Channel channel, Message msg) {
        for (BlogIndexSupport handler : elasticsearchHandlers) {
            if (handler.supports(message.typeEnum())) {
                handler.handle(message, channel, msg);
                break;
            }
        }
    }
}
