package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import static wiki.chiu.micro.common.lang.Const.*;

import java.util.HashSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.exhibit.adapter.in.messaging.cache.CacheKeyGenerator;
import wiki.chiu.micro.exhibit.cache.BlogCacheDescriptors;

@Component
public final class DeleteBlogCacheEvictHandler extends BlogCacheEvictHandler {

  private final CacheKeyGenerator cacheKeyGenerator;

  private final CacheKeyFactory cacheKeyFactory;

  public DeleteBlogCacheEvictHandler(
      RedissonClient redissonClient,
      CacheKeyGenerator cacheKeyGenerator,
      CacheEvictor cacheEvictor,
      CacheKeyFactory cacheKeyFactory) {
    super(redissonClient, cacheEvictor);
    this.cacheKeyGenerator = cacheKeyGenerator;
    this.cacheKeyFactory = cacheKeyFactory;
  }

  @Override
  public boolean supports(BlogOperateEnum blogOperateEnum) {
    return BlogOperateEnum.REMOVE.equals(blogOperateEnum);
  }

  @Override
  public void redisProcess(BlogChangedMessage message) {
    BlogEntityRpcVo blogEntity = blogEntity(message.blogSnapshot());
    Long id = blogEntity.id();

    long pageInvalidationCount =
        message.previousTotalCount() == null
            ? message.totalCount()
            : message.previousTotalCount();

    evictCaches(id, pageInvalidationCount);
    clearKeys(id);
    setDetailBloom(id);
    deleteHotRead(id);
  }

  private void deleteHotRead(Long id) {
    redissonClient.getScoredSortedSet(HOT_READ).remove(id.toString());
  }

  private void setDetailBloom(Long id) {
    redissonClient.getBitSet(BLOOM_FILTER_BLOG).set(id, false);
  }

  private void clearKeys(Long id) {
    HashSet<String> clearKeys = new HashSet<>();
    clearKeys.add(READ_TOKEN + id);
    redissonClient.getKeys().delete(clearKeys.toArray(new String[0]));
  }

  private void evictCaches(Long id, long count) {
    HashSet<String> keys = new HashSet<>();

    keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.DETAIL, id));
    keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.SENSITIVE, id));
    keys.addAll(cacheKeyGenerator.generateHotBlogsKeys(count));
    cacheEvictor.evict(keys);
  }
}
