package org.chiu.micro.auth.config;

import org.chiu.micro.auth.cache.local.CacheEvictMessageListener;
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
public class CacheUserEvictRabbitConfig {

    private String cacheEvictQueue = "cache.user.evict.queue.";

    public static final String CACHE_EVICT_FANOUT_EXCHANGE = "cache.user.evict.fanout.exchange";

    private final Jackson2JsonMessageConverter jsonMessageConverter;

    @Qualifier("mqExecutor")
    private final TaskExecutor executor;

    public CacheUserEvictRabbitConfig(Jackson2JsonMessageConverter jsonMessageConverter, @Qualifier("mqExecutor") TaskExecutor executor) {
        this.jsonMessageConverter = jsonMessageConverter;
        this.executor = executor;
    }

    @Bean("cacheEvictQueue")
    Queue evictQueue() {
        String evictNodeMark = UUID.randomUUID().toString();
        cacheEvictQueue += evictNodeMark;
        return new Queue(cacheEvictQueue, false, true, true);
    }

    @Bean("cacheEvictFanoutExchange")
    FanoutExchange evictExchange() {
        return new FanoutExchange(CACHE_EVICT_FANOUT_EXCHANGE, true, false);
    }

    @Bean("cacheEvictBinding")
    Binding evictBinding(@Qualifier("cacheEvictQueue") Queue cacheQueue,
                         @Qualifier("cacheEvictFanoutExchange") FanoutExchange cacheExchange) {
        return BindingBuilder
                .bind(cacheQueue)
                .to(cacheExchange);
    }

    @Bean("cacheEvictMessageListenerAdapter")
    MessageListenerAdapter coopMessageListener(CacheEvictMessageListener cacheEvictMessageListener) {
        return new MessageListenerAdapter(cacheEvictMessageListener);
    }

    @Bean("cacheEvictMessageListenerContainer")
    SimpleMessageListenerContainer cacheEvictMessageListenerContainer(ConnectionFactory connectionFactory,
                                                                      @Qualifier("cacheEvictMessageListenerAdapter") MessageListenerAdapter listenerAdapter,
                                                                      @Qualifier("cacheEvictQueue") Queue queue) {
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
