package wiki.chiu.micro.cache.handler.impl;

import com.github.benmanes.caffeine.cache.Cache;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

public final class RedisCacheEvictor extends AbstractCacheEvictor {

  private final RedissonClient redissonClient;
  private final JsonMapper jsonMapper;
  private final String topic;

  public RedisCacheEvictor(
      RedissonClient redissonClient,
      JsonMapper jsonMapper,
      Cache<@NonNull String, LocalCacheEntry> localCache,
      CacheProperties properties,
      CacheMetrics metrics) {
    super(redissonClient, localCache, properties, metrics);
    this.redissonClient = redissonClient;
    this.jsonMapper = jsonMapper;
    this.topic = properties.getEviction().getRedis().getTopic();
  }

  @Override
  protected String transport() {
    return "redis";
  }

  @Override
  protected void broadcast(CacheEvictionMessage message) {
    String value = jsonMapper.writeValueAsString(message);
    redissonClient.getReliableTopic(topic, StringCodec.INSTANCE).publish(value);
  }
}
