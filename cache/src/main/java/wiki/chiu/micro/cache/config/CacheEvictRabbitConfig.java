package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.handler.impl.RabbitCacheEvictHandler;
import wiki.chiu.micro.cache.listener.RabbitCacheEvictMessageListener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.UUID;

@AutoConfiguration
@ConditionalOnClass(ConnectionFactory.class)
public class CacheEvictRabbitConfig {

    @Value("${megalith.cache.queue-prefix}")
    private String cacheEvictQueue;

    @Value("${megalith.cache.fanout-exchange}")
    public String CACHE_EVICT_FANOUT_EXCHANGE;

    private final Cache<String, Object> localCache;

    private final RedissonClient redissonClient;

    private final RabbitTemplate rabbitTemplate;

    public CacheEvictRabbitConfig(@Qualifier("caffeineCache") Cache<String, Object> localCache, @Qualifier("cacheRedissonClient") RedissonClient redissonClient, @Qualifier("cacheRabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.localCache = localCache;
        this.redissonClient = redissonClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean("cacheEvictQueue")
    Queue evictQueue() {
        String evictNodeMark = UUID.randomUUID().toString();
        cacheEvictQueue += evictNodeMark;
        return new Queue(cacheEvictQueue, false, false, true);
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
    MessageListenerAdapter coopMessageListener() {
        RabbitCacheEvictMessageListener rabbitCacheEvictMessageListener = new RabbitCacheEvictMessageListener(localCache);
        return new MessageListenerAdapter(rabbitCacheEvictMessageListener);
    }

    @Bean("cacheEvictMessageListenerContainer")
    SimpleMessageListenerContainer cacheEvictMessageListenerContainer(ConnectionFactory connectionFactory,
                                                                      @Qualifier("cacheEvictMessageListenerAdapter") MessageListenerAdapter listenerAdapter,
                                                                      @Qualifier("cacheEvictQueue") Queue queue) {
        var container = new SimpleMessageListenerContainer();
        //框架处理了
        listenerAdapter.containerAckMode(AcknowledgeMode.MANUAL);
        listenerAdapter.setMessageConverter(new Jackson2JsonMessageConverter());
        container.setConcurrency("5");
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queue.getName());
        container.setMessageListener(listenerAdapter);
        container.setTaskExecutor(simpleAsyncTaskExecutor());
        return container;
    }

    TaskExecutor simpleAsyncTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        return executor;
    }

    @Bean
    CacheEvictHandler rabbitCacheEvictHandler() {
        return new RabbitCacheEvictHandler(rabbitTemplate, redissonClient, CACHE_EVICT_FANOUT_EXCHANGE);
    }
}
