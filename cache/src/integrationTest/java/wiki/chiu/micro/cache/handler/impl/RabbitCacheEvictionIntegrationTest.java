package wiki.chiu.micro.cache.handler.impl;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.listener.RabbitCacheEvictMessageListener;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

@Testcontainers(disabledWithoutDocker = true)
class RabbitCacheEvictionIntegrationTest {

  @Container
  private static final RabbitMQContainer RABBIT =
      new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.3.4-management"));

  @Test
  void confirmsFanoutAndInvalidatesEveryReplica() throws Exception {
    CachingConnectionFactory connectionFactory = connectionFactory();
    String exchangeName = "cache-integration-" + UUID.randomUUID();
    FanoutExchange exchange = new FanoutExchange(exchangeName, false, true);
    Queue firstQueue = new Queue(exchangeName + ".first", false, true, true);
    Queue secondQueue = new Queue(exchangeName + ".second", false, true, true);
    RabbitAdmin admin = new RabbitAdmin(connectionFactory);
    admin.declareExchange(exchange);
    admin.declareQueue(firstQueue);
    admin.declareQueue(secondQueue);
    admin.declareBinding(BindingBuilder.bind(firstQueue).to(exchange));
    admin.declareBinding(BindingBuilder.bind(secondQueue).to(exchange));

    Cache<String, LocalCacheEntry> publisherLocal = Caffeine.newBuilder().build();
    Cache<String, LocalCacheEntry> firstLocal = Caffeine.newBuilder().build();
    Cache<String, LocalCacheEntry> secondLocal = Caffeine.newBuilder().build();
    SimpleMessageListenerContainer firstContainer =
        listenerContainer(connectionFactory, firstQueue, firstLocal);
    SimpleMessageListenerContainer secondContainer =
        listenerContainer(connectionFactory, secondQueue, secondLocal);
    String key = "integration:v1:key";

    try {
      firstContainer.start();
      secondContainer.start();
      publisherLocal.put(key, entry());
      firstLocal.put(key, entry());
      secondLocal.put(key, entry());

      CacheProperties properties = new CacheProperties();
      properties.getEviction().getRabbit().setExchange(exchangeName);
      RabbitCacheEvictor evictor =
          new RabbitCacheEvictor(
              rabbitTemplate(connectionFactory),
              redisson(),
              publisherLocal,
              properties,
              new CacheMetrics(null));

      evictor.evict(Set.of(key));

      await()
          .atMost(Duration.ofSeconds(5))
          .until(
              () ->
                  publisherLocal.getIfPresent(key) == null
                      && firstLocal.getIfPresent(key) == null
                      && secondLocal.getIfPresent(key) == null);
    } finally {
      secondContainer.stop();
      firstContainer.stop();
      connectionFactory.destroy();
    }
  }

  private CachingConnectionFactory connectionFactory() {
    CachingConnectionFactory result =
        new CachingConnectionFactory(RABBIT.getHost(), RABBIT.getAmqpPort());
    result.setUsername(RABBIT.getAdminUsername());
    result.setPassword(RABBIT.getAdminPassword());
    result.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
    result.setPublisherReturns(true);
    return result;
  }

  private RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
    RabbitTemplate result = new RabbitTemplate(connectionFactory);
    result.setMandatory(true);
    result.setMessageConverter(messageConverter());
    return result;
  }

  private SimpleMessageListenerContainer listenerContainer(
      CachingConnectionFactory connectionFactory,
      Queue queue,
      Cache<String, LocalCacheEntry> localCache) {
    MessageListenerAdapter adapter =
        new MessageListenerAdapter(new RabbitCacheEvictMessageListener(localCache));
    adapter.setMessageConverter(messageConverter());
    SimpleMessageListenerContainer result =
        new SimpleMessageListenerContainer(connectionFactory);
    result.setQueueNames(queue.getName());
    result.setMessageListener(adapter);
    return result;
  }

  private JacksonJsonMessageConverter messageConverter() {
    return new JacksonJsonMessageConverter(CacheEvictionMessage.class.getPackageName());
  }

  private RedissonClient redisson() throws Exception {
    RedissonClient result = mock(RedissonClient.class);
    RLock lock = mock(RLock.class);
    RKeys keys = mock(RKeys.class);
    when(result.getLock("megalithRemoteLock:integration:v1:key")).thenReturn(lock);
    when(lock.tryLock(5000, TimeUnit.MILLISECONDS)).thenReturn(true);
    when(result.getKeys()).thenReturn(keys);
    return result;
  }

  private LocalCacheEntry entry() {
    return new LocalCacheEntry("local", Duration.ofMinutes(1));
  }
}
