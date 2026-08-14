package wiki.chiu.micro.exhibit.consumer.cache.handler;

import java.time.Duration;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogSnapshot;

public abstract sealed class BlogCacheEvictHandler
    permits CreateBlogCacheEvictHandler, DeleteBlogCacheEvictHandler, UpdateBlogCacheEvictHandler {

  protected final RedissonClient redissonClient;

  protected final CacheEvictHandler cacheEvictHandler;

  protected BlogCacheEvictHandler(
      RedissonClient redissonClient, CacheEvictHandler cacheEvictHandler) {
    this.redissonClient = redissonClient;
    this.cacheEvictHandler = cacheEvictHandler;
  }

  public abstract boolean supports(BlogOperateEnum blogOperateEnum);

  protected abstract void redisProcess(BlogChangedMessage message);

  public void process(BlogChangedMessage message) {
    Long blogId = message.blogSnapshot().id();
    RLock lock = redissonClient.getLock("blog:event-revision:lock:" + blogId);
    try {
      lock.lock();
      RBucket<String> revisionBucket = redissonClient.getBucket("blog:event-revision:" + blogId);
      String applied = revisionBucket.get();
      if (applied == null || Long.parseLong(applied) < message.revision()) {
        redisProcess(message);
        revisionBucket.set(message.revision().toString(), Duration.ofDays(30));
      }
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  protected BlogEntityRpcVo blogEntity(BlogSnapshot blog) {
    return new BlogEntityRpcVo(
        blog.id(),
        blog.userId(),
        blog.title(),
        blog.description(),
        blog.content(),
        blog.created(),
        blog.updated(),
        blog.status(),
        blog.link(),
        blog.readCount());
  }
}
