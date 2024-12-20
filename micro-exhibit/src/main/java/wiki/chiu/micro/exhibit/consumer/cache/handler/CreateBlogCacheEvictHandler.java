package wiki.chiu.micro.exhibit.consumer.cache.handler;

import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.utils.KeyUtils;
import wiki.chiu.micro.exhibit.consumer.cache.CacheKeyGenerator;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;

import static wiki.chiu.micro.common.lang.Const.*;

@Component
public final class CreateBlogCacheEvictHandler extends BlogCacheEvictHandler {

    private final CacheKeyGenerator cacheKeyGenerator;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public CreateBlogCacheEvictHandler(RedissonClient redissonClient,
                                       BlogHttpServiceWrapper blogHttpServiceWrapper,
                                       CacheKeyGenerator cacheKeyGenerator,
                                       CacheEvictHandler cacheEvictHandler) {
        super(redissonClient, blogHttpServiceWrapper, cacheEvictHandler);
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.CREATE.equals(blogOperateEnum);
    }


    @Override
    public void redisProcess(BlogEntityRpcDto blogEntity) {
        Long id = blogEntity.id();
        int year = blogEntity.created().getYear();
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        long count = blogHttpServiceWrapper.count();
        long countYear = blogHttpServiceWrapper.countByCreatedBetween(start, end);

        evictCaches(year, count, countYear);
        deleteBlogEditKey(blogEntity.userId());
        rebuildYearPageBloom(year, countYear);
        rebuildPageBloom(count);
        setBlogDetailBloom(id);
        updateYearFilterBloom(year);
    }

    private void evictCaches(int year, long count, long countYear) {
        HashSet<String> keys = cacheKeyGenerator.generateHotBlogsKeys(year, count, countYear);
        cacheEvictHandler.evictCache(keys);
    }

    private void deleteBlogEditKey(Long userId) {
        String blogEditKey = KeyUtils.createBlogEditRedisKey(userId, null);
        redissonClient.getBucket(blogEditKey).delete();
    }

    private void rebuildYearPageBloom(int year, long countYear) {
        int totalPageByPeriod = (int) (countYear % blogPageSize == 0 ? countYear / blogPageSize : countYear / blogPageSize + 1);
        for (int i = 1; i <= totalPageByPeriod; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_YEAR_PAGE + year).set(i, true);
        }
    }

    private void rebuildPageBloom(long count) {
        int totalPage = (int) (count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1);
        for (int i = 1; i <= totalPage; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_PAGE).set(i, true);
        }
    }

    private void setBlogDetailBloom(Long id) {
        redissonClient.getBitSet(BLOOM_FILTER_BLOG).set(id, true);
    }

    private void updateYearFilterBloom(int year) {
        redissonClient.getBitSet(BLOOM_FILTER_YEARS).set(year, true);
    }
}
