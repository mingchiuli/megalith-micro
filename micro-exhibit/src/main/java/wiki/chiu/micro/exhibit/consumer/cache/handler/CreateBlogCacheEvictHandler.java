package wiki.chiu.micro.exhibit.consumer.cache.handler;

import static wiki.chiu.micro.common.lang.Const.*;

import java.util.HashSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.exhibit.consumer.cache.CacheKeyGenerator;

@Component
public final class CreateBlogCacheEvictHandler extends BlogCacheEvictHandler {

  private final CacheKeyGenerator cacheKeyGenerator;

  @Value("${megalith.blog.blog-page-size}")
  private int blogPageSize;

  public CreateBlogCacheEvictHandler(
      RedissonClient redissonClient,
      CacheKeyGenerator cacheKeyGenerator,
      CacheEvictor cacheEvictor) {
    super(redissonClient, cacheEvictor);
    this.cacheKeyGenerator = cacheKeyGenerator;
  }

  @Override
  public boolean supports(BlogOperateEnum blogOperateEnum) {
    return BlogOperateEnum.CREATE.equals(blogOperateEnum);
  }

  @Override
  public void redisProcess(BlogChangedMessage message) {
    BlogEntityRpcVo blogEntity = blogEntity(message.blogSnapshot());
    Long id = blogEntity.id();
    long count = message.totalCount();

    evictCaches(count);
    rebuildPageBloom(count);
    setBlogDetailBloom(id);
  }

  private void evictCaches(long count) {
    HashSet<String> keys = cacheKeyGenerator.generateHotBlogsKeys(count);
    cacheEvictor.evict(keys);
  }

  private void rebuildPageBloom(long count) {
    int totalPage =
        (int) (count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1);
    for (int i = 1; i <= totalPage; i++) {
      redissonClient.getBitSet(BLOOM_FILTER_PAGE).set(i, true);
    }
  }

  private void setBlogDetailBloom(Long id) {
    redissonClient.getBitSet(BLOOM_FILTER_BLOG).set(id, true);
  }
}
