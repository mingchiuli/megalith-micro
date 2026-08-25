package wiki.chiu.micro.cache.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.redisson.api.RReliableTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.handler.impl.RedisCacheEvictor;

class CacheAutoConfigurationTest {

  @Test
  void startsWithApplicationRedissonAndExplicitRedisTransport() {
    RedissonClient redisson = mock(RedissonClient.class);
    RReliableTopic topic = mock(RReliableTopic.class);
    when(redisson.getReliableTopic(anyString(), eq(StringCodec.INSTANCE))).thenReturn(topic);
    when(topic.addListener(eq(String.class), org.mockito.ArgumentMatchers.any())).thenReturn("id");

    contextRunner()
        .withPropertyValues("megalith.cache.eviction.transport=REDIS")
        .withBean(RedissonClient.class, () -> redisson)
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(CacheEvictor.class);
              assertThat(context.getBean(CacheEvictor.class)).isInstanceOf(RedisCacheEvictor.class);
            });
  }

  @Test
  void failsFastWithoutAnApplicationRedissonClient() {
    contextRunner()
        .withPropertyValues("megalith.cache.eviction.transport=REDIS")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void explicitRabbitFailsWhenNoConnectionFactoryExists() {
    RedissonClient redisson = mock(RedissonClient.class);

    contextRunner()
        .withPropertyValues(
            "megalith.cache.eviction.transport=RABBIT",
            "megalith.cache.eviction.rabbit.queue-prefix=cache.queue.",
            "megalith.cache.eviction.rabbit.exchange=cache.exchange")
        .withBean(RedissonClient.class, () -> redisson)
        .run(context -> assertThat(context).hasFailed());
  }

  private ApplicationContextRunner contextRunner() {
    return new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                CacheAspectConfig.class,
                CacheEvictRabbitConfig.class,
                CacheEvictRedisConfig.class,
                CacheEvictionConfig.class,
                LocalCacheConfig.class))
        .withBean(JsonMapper.class, () -> JsonMapper.builder().build());
  }
}
