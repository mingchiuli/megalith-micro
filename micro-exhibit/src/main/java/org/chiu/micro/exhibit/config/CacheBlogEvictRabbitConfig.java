package org.chiu.micro.exhibit.config;

import lombok.RequiredArgsConstructor;
import org.chiu.micro.exhibit.cache.mq.CacheBlogEvictMessageListener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class CacheBlogEvictRabbitConfig {

    private String evictNodeMark;

    private String cacheBlogEvictQueue = "cache.blog.evict.queue.";

    public static final String CACHE_BLOG_EVICT_FANOUT_EXCHANGE = "cache.blog.evict.fanout.exchange";

    private final Jackson2JsonMessageConverter jsonMessageConverter;

    @Qualifier("mqExecutor")
    private final TaskExecutor executor;

    @Bean("cacheBlogEvictFanoutQueue")
    Queue evictQueue() {
        evictNodeMark = UUID.randomUUID().toString();
        cacheBlogEvictQueue += evictNodeMark;
        return new Queue(cacheBlogEvictQueue, false, true, true);
    }

    @Bean("cacheBlogEvictFanoutExchange")
    FanoutExchange evictExchange() {
        return new FanoutExchange(CACHE_BLOG_EVICT_FANOUT_EXCHANGE, true, false);
    }

    @Bean("cacheBlogEvictFanoutBinding")
    Binding evictBinding(@Qualifier("cacheBlogEvictFanoutQueue") Queue cacheQueue,
                         @Qualifier("cacheBlogEvictFanoutExchange") FanoutExchange cacheExchange) {
        return BindingBuilder
                .bind(cacheQueue)
                .to(cacheExchange);
    }

    @Bean("cacheBlogEvictMessageListenerAdapter")
    MessageListenerAdapter coopMessageListener(CacheBlogEvictMessageListener cacheBlogEvictMessageListener) {
        return new MessageListenerAdapter(cacheBlogEvictMessageListener);
    }

    @Bean("cacheBlogEvictMessageListenerContainer")
    SimpleMessageListenerContainer cacheBlogEvictMessageListenerContainer(ConnectionFactory connectionFactory,
                                                                          @Qualifier("cacheBlogEvictMessageListenerAdapter") MessageListenerAdapter listenerAdapter,
                                                                          @Qualifier("cacheBlogEvictFanoutQueue") Queue queue) {
        var container = new SimpleMessageListenerContainer();
        //框架处理了
        listenerAdapter.containerAckMode(AcknowledgeMode.MANUAL);
        listenerAdapter.setMessageConverter(jsonMessageConverter);
        container.setConcurrency("5");
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queue.getName());
        container.setMessageListener(listenerAdapter);
        container.setTaskExecutor(executor);
        return container;
    }
}
