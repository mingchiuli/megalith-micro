package wiki.chiu.micro.exhibit.adapter.in.messaging;

import java.util.HashSet;
import java.util.Set;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.exhibit.application.port.in.BlogChangeMaintenance;
import wiki.chiu.micro.exhibit.domain.PublishedContentCache;

/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
public class BlogChangeMessageListener {

    private static final CacheDescriptor CONTENT =
        new CacheDescriptor(PublishedContentCache.NAMESPACE, PublishedContentCache.VERSION);

    private final CacheKeyFactory keys;
    private final CacheEvictor evictor;
    private final BlogChangeMaintenance blogChangeMaintenance;
    private final RetryingMessageRecoverer recoverer;

    public BlogChangeMessageListener(
        BlogChangeMaintenance blogChangeMaintenance, RetryingMessageRecoverer recoverer,
        CacheKeyFactory keys, CacheEvictor evictor) {
        this.keys = keys;
        this.evictor = evictor;
        this.blogChangeMaintenance = blogChangeMaintenance;
        this.recoverer = recoverer;
    }

    @RabbitListener(
        queues = Const.EXHIBIT_STATE_QUEUE,
        concurrency = "10",
        messageConverter = "jsonMessageConverter",
        executor = "mqExecutor")
    public void handler(BlogChangedMessage message, Channel channel, Message msg) {
        try {
            blogChangeMaintenance.apply(BlogMessageConvertor.toCommand(message));
            // Even late events can name a superseded version that needs eviction.
            evictContent(message);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception failure) {
            recoverer.recover(msg, channel, failure);
        }
    }

    private void evictContent(BlogChangedMessage event) {
        var snapshot = event.blogSnapshot();
        Set<String> affected = new HashSet<>();
        if (snapshot.publishedRevisionId() != null) {
            affected.add(keys.generate(CONTENT, snapshot.id(), snapshot.publishedRevisionId()));
        }
        if (event.previousPublishedRevisionId() != null) {
            affected.add(keys.generate(CONTENT, snapshot.id(), event.previousPublishedRevisionId()));
        }
        if (!affected.isEmpty()) {
            evictor.evict(affected);
        }
    }
}
