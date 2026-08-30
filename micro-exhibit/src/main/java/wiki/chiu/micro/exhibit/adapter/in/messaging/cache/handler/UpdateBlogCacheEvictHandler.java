package wiki.chiu.micro.exhibit.adapter.in.messaging.cache.handler;

import static wiki.chiu.micro.common.lang.BlogStatusEnum.NORMAL;
import static wiki.chiu.micro.common.lang.Const.READ_TOKEN;

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
public final class UpdateBlogCacheEvictHandler extends BlogCacheEvictHandler {

  private final CacheKeyGenerator cacheKeyGenerator;

  private final CacheKeyFactory cacheKeyFactory;

  public UpdateBlogCacheEvictHandler(
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
    return BlogOperateEnum.UPDATE.equals(blogOperateEnum);
  }

  @Override
  public void redisProcess(BlogChangedMessage message) {
    BlogEntityRpcVo blogEntity = blogEntity(message.blogSnapshot());
    Long id = blogEntity.id();
    Integer status = blogEntity.status();

    // 保守处理，前面的全删
    long countAfter = message.newerOrSameCount();
    evictCaches(id, countAfter);
    clearReadToken(id, status);
  }

  private void evictCaches(Long id, long countAfter) {
    HashSet<String> keys = cacheKeyGenerator.generateBlogKey(countAfter);

    keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.DETAIL, id));
    keys.add(cacheKeyFactory.generate(BlogCacheDescriptors.SENSITIVE, id));
    cacheEvictor.evict(keys);
  }

  private void clearReadToken(Long id, Integer status) {
    if (NORMAL.getCode().equals(status)) {
      redissonClient.getKeys().delete(READ_TOKEN + id);
    }
  }
}
