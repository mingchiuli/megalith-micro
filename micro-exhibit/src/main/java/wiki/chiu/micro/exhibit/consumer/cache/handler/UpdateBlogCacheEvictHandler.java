package wiki.chiu.micro.exhibit.consumer.cache.handler;

import static wiki.chiu.micro.common.lang.BlogStatusEnum.NORMAL;
import static wiki.chiu.micro.common.lang.Const.READ_TOKEN;

import java.lang.reflect.Method;
import java.util.HashSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.exhibit.consumer.cache.CacheKeyGenerator;
import wiki.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogWrapper;

@Component
public final class UpdateBlogCacheEvictHandler extends BlogCacheEvictHandler {

  private static final Logger log = LoggerFactory.getLogger(UpdateBlogCacheEvictHandler.class);
  private final CacheKeyGenerator cacheKeyGenerator;

  private final CommonCacheKeyGenerator commonCacheKeyGenerator;

  public UpdateBlogCacheEvictHandler(
      RedissonClient redissonClient,
      CacheKeyGenerator cacheKeyGenerator,
      CacheEvictHandler cacheEvictHandler,
      CommonCacheKeyGenerator commonCacheKeyGenerator) {
    super(redissonClient, cacheEvictHandler);
    this.cacheKeyGenerator = cacheKeyGenerator;
    this.commonCacheKeyGenerator = commonCacheKeyGenerator;
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

    // 博客对象本身缓存
    try {
      Method findByIdAndVisibleMethod = BlogWrapper.class.getMethod("findById", Long.class);
      String findByIdAndVisible = commonCacheKeyGenerator.generateKey(findByIdAndVisibleMethod, id);
      keys.add(findByIdAndVisible);
    } catch (NoSuchMethodException e) {
      log.error(e.getMessage());
    }

    try {
      Method sensitiveMethod =
          BlogSensitiveWrapper.class.getMethod("findSensitiveByBlogId", Long.class);
      String sensitive = commonCacheKeyGenerator.generateKey(sensitiveMethod, id);
      keys.add(sensitive);
    } catch (NoSuchMethodException e) {
      log.error(e.getMessage());
    }

    cacheEvictHandler.evictCache(keys);
  }

  private void clearReadToken(Long id, Integer status) {
    if (NORMAL.getCode().equals(status)) {
      redissonClient.getKeys().delete(READ_TOKEN + id);
    }
  }
}
