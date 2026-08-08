package wiki.chiu.micro.cache.handler;

import java.util.HashSet;
import org.redisson.api.RedissonClient;

public abstract class CacheEvictHandler {

  protected final RedissonClient redissonClient;

  protected CacheEvictHandler(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  public abstract void evictCache(HashSet<String> keys);
}
