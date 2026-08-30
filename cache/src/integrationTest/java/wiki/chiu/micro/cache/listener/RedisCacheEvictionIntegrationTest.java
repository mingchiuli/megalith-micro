package wiki.chiu.micro.cache.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.handler.impl.RedisCacheEvictor;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

@Testcontainers(disabledWithoutDocker = true)
class RedisCacheEvictionIntegrationTest {

  @Container
  private static final GenericContainer<?> REDIS = redisContainer();

  private static GenericContainer<?> redisContainer() {
    GenericContainer<?> result =
        new GenericContainer<>(DockerImageName.parse("redis:8.10.0"));
    result.addExposedPort(6379);
    return result;
  }

  @Test
  void broadcastsExactEvictionToEveryReplica() {
    RedissonClient firstClient = redissonClient();
    RedissonClient secondClient = redissonClient();
    JsonMapper jsonMapper = JsonMapper.builder().build();
    CacheProperties properties = new CacheProperties();
    properties.getEviction().getRedis().setTopic("cache-integration-redis");
    Cache<String, LocalCacheEntry> firstLocal = Caffeine.newBuilder().build();
    Cache<String, LocalCacheEntry> secondLocal = Caffeine.newBuilder().build();
    RedisCacheEvictMessageListener firstListener =
        new RedisCacheEvictMessageListener(
            properties.getEviction().getRedis().getTopic(), firstClient, jsonMapper, firstLocal);
    RedisCacheEvictMessageListener secondListener =
        new RedisCacheEvictMessageListener(
            properties.getEviction().getRedis().getTopic(), secondClient, jsonMapper, secondLocal);
    String key = "integration:v1:key";

    try {
      firstListener.start();
      secondListener.start();
      firstLocal.put(key, entry());
      secondLocal.put(key, entry());
      firstClient.getBucket(key, StringCodec.INSTANCE).set("\"remote\"");
      RedisCacheEvictor evictor =
          new RedisCacheEvictor(
              firstClient,
              jsonMapper,
              firstLocal,
              properties,
              new CacheMetrics(null));

      evictor.evict(Set.of(key));

      assertThat(firstClient.getBucket(key, StringCodec.INSTANCE).isExists()).isFalse();
      assertThat(firstLocal.getIfPresent(key)).isNull();
      await().atMost(Duration.ofSeconds(5)).until(() -> secondLocal.getIfPresent(key) == null);
    } finally {
      secondListener.stop();
      firstListener.stop();
      secondClient.shutdown();
      firstClient.shutdown();
    }
  }

  private RedissonClient redissonClient() {
    Config config = new Config();
    config
        .useSingleServer()
        .setAddress("redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
    return Redisson.create(config);
  }

  private LocalCacheEntry entry() {
    return new LocalCacheEntry("local", Duration.ofMinutes(1));
  }
}
