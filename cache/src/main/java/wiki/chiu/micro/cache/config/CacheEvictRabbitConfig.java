package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.handler.impl.RabbitCacheEvictor;
import wiki.chiu.micro.cache.listener.RabbitCacheEvictMessageListener;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

@AutoConfiguration
@ConditionalOnClass({ConnectionFactory.class, RabbitAutoConfiguration.class})
@ConditionalOnBean(ConnectionFactory.class)
@Conditional(CacheRabbitTransportCondition.class)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
public class CacheEvictRabbitConfig {

  @Bean("cacheEvictQueue")
  Queue cacheEvictQueue(CacheProperties properties) {
    String prefix = requireText(properties.getEviction().getRabbit().getQueuePrefix(), "queue-prefix");
    return new Queue(prefix + UUID.randomUUID(), false, true, true);
  }

  @Bean("cacheEvictFanoutExchange")
  FanoutExchange cacheEvictFanoutExchange(CacheProperties properties) {
    String exchange = requireText(properties.getEviction().getRabbit().getExchange(), "exchange");
    return new FanoutExchange(exchange, true, false);
  }

  @Bean("cacheEvictBinding")
  Binding cacheEvictBinding(
      @Qualifier("cacheEvictQueue") Queue queue,
      @Qualifier("cacheEvictFanoutExchange") FanoutExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange);
  }

  @Bean("cacheRabbitTemplate")
  RabbitTemplate cacheRabbitTemplate(
      ConnectionFactory connectionFactory,
      @Qualifier("cacheEvictMessageConverter") JacksonJsonMessageConverter messageConverter) {
    if (!connectionFactory.isPublisherConfirms() || !connectionFactory.isPublisherReturns()) {
      throw new IllegalStateException(
          "Rabbit cache eviction requires publisher confirms and publisher returns");
    }
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMandatory(true);
    rabbitTemplate.setMessageConverter(messageConverter);
    return rabbitTemplate;
  }

  @Bean("cacheEvictMessageConverter")
  JacksonJsonMessageConverter cacheEvictMessageConverter(JsonMapper jsonMapper) {
    return new JacksonJsonMessageConverter(
        jsonMapper, CacheEvictionMessage.class.getPackageName());
  }

  @Bean("cacheEvictConnectionListener")
  ConnectionListener cacheEvictConnectionListener(
      ConnectionFactory connectionFactory,
      @Qualifier("caffeineCache") Cache<@NonNull String, LocalCacheEntry> localCache) {
    ConnectionListener listener = _ -> localCache.invalidateAll();
    connectionFactory.addConnectionListener(listener);
    return listener;
  }

  @Bean("cacheEvictMessageListenerAdapter")
  MessageListenerAdapter cacheEvictMessageListenerAdapter(
      @Qualifier("caffeineCache") Cache<@NonNull String, LocalCacheEntry> localCache,
      @Qualifier("cacheEvictMessageConverter") JacksonJsonMessageConverter messageConverter) {
    MessageListenerAdapter adapter =
        new MessageListenerAdapter(new RabbitCacheEvictMessageListener(localCache));
    adapter.setMessageConverter(messageConverter);
    return adapter;
  }

  @Bean("cacheEvictMessageListenerContainer")
  SimpleMessageListenerContainer cacheEvictMessageListenerContainer(
      ConnectionFactory connectionFactory,
      @Qualifier("cacheEvictMessageListenerAdapter") MessageListenerAdapter listenerAdapter,
      @Qualifier("cacheEvictQueue") Queue queue,
      @Qualifier("cacheEvictConnectionListener") ConnectionListener connectionListener) {
    var container = new SimpleMessageListenerContainer(connectionFactory);
    container.setAcknowledgeMode(AcknowledgeMode.AUTO);
    container.setConcurrentConsumers(1);
    container.setQueueNames(queue.getName());
    container.setMessageListener(listenerAdapter);
    return container;
  }

  @Bean
  CacheEvictor rabbitCacheEvictor(
      @Qualifier("cacheRabbitTemplate") RabbitTemplate rabbitTemplate,
      RedissonClient redissonClient,
      @Qualifier("caffeineCache") Cache<@NonNull String, LocalCacheEntry> localCache,
      CacheProperties properties,
      CacheMetrics metrics) {
    return new RabbitCacheEvictor(
        rabbitTemplate, redissonClient, localCache, properties, metrics);
  }

  private String requireText(String value, String property) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(
          "megalith.cache.eviction.rabbit." + property + " must not be blank");
    }
    return value;
  }
}
