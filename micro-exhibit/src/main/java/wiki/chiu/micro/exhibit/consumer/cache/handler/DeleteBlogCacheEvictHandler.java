package wiki.chiu.micro.exhibit.consumer.cache.handler;

import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.utils.KeyUtils;
import wiki.chiu.micro.exhibit.consumer.cache.CacheKeyGenerator;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static wiki.chiu.micro.common.lang.Const.*;


@Component
public final class DeleteBlogCacheEvictHandler extends BlogCacheEvictHandler {


    private static final Logger log = LoggerFactory.getLogger(DeleteBlogCacheEvictHandler.class);

    private final CacheKeyGenerator cacheKeyGenerator;

    private final CommonCacheKeyGenerator commonCacheKeyGenerator;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    public DeleteBlogCacheEvictHandler(RedissonClient redissonClient,
                                       BlogHttpServiceWrapper blogHttpServiceWrapper,
                                       CacheKeyGenerator cacheKeyGenerator,
                                       CacheEvictHandler cacheEvictHandler,
                                       CommonCacheKeyGenerator commonCacheKeyGenerator) {
        super(redissonClient, blogHttpServiceWrapper, cacheEvictHandler);
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.commonCacheKeyGenerator = commonCacheKeyGenerator;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.REMOVE.equals(blogOperateEnum);
    }

    @Override
    public void redisProcess(BlogEntityRpcDto blogEntity) {
        Long id = blogEntity.id();
        int year = blogEntity.created().getYear();
        Long userId = blogEntity.userId();

        long count = blogHttpServiceWrapper.count();
        var start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        var end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        long countYear = blogHttpServiceWrapper.countByCreatedBetween(start, end);

        evictCaches(id, year, count, countYear);
        clearKeys(id, year, userId);
        setDetailBloom(id);
        rebuildYearPageBloom(year, countYear);
        rebuildPageBloom(count);
        rebuildYearsBloom();
        deleteHotRead(id);
    }

    private void deleteHotRead(Long id) {
        redissonClient.getScoredSortedSet(HOT_READ).remove(id.toString());
    }

    private void rebuildYearsBloom() {
        List<Integer> years = blogHttpServiceWrapper.getYears();
        years.forEach(y -> redissonClient.getBitSet(BLOOM_FILTER_YEARS).set(y, true));
    }

    private void rebuildPageBloom(long count) {
        int totalPage = (int) (count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1);
        for (int i = 1; i <= totalPage; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_PAGE).set(i, true);
        }
    }

    private void rebuildYearPageBloom(int year, long countYear) {
        int totalPageByPeriod = (int) (countYear % blogPageSize == 0 ? countYear / blogPageSize : countYear / blogPageSize + 1);
        for (int i = 1; i <= totalPageByPeriod; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_YEAR_PAGE + year).set(i, true);
        }
    }

    private void setDetailBloom(Long id) {
        redissonClient.getBitSet(BLOOM_FILTER_BLOG).set(id, false);
    }

    private void clearKeys(Long id, Integer year, Long userId) {
        HashSet<String> clearKeys = new HashSet<>();
        clearKeys.add(READ_TOKEN + id);
        //删除该年份的页面bloom，listPage的bloom，getCountByYear的bloom，后面逻辑重建
        clearKeys.add(BLOOM_FILTER_YEAR_PAGE + year);
        clearKeys.add(BLOOM_FILTER_PAGE);
        clearKeys.add(BLOOM_FILTER_YEARS);
        //暂存区
        clearKeys.add(KeyUtils.createBlogEditRedisKey(userId, id));
        redissonClient.getKeys().delete(clearKeys.toArray(new String[0]));
    }

    private void evictCaches(Long id, int year, long count, long countYear) {
        HashSet<String> keys = new HashSet<>();

        //博客对象本身缓存
        try {
            Method findByIdMethod = BlogWrapper.class.getMethod("findById", Long.class);
            String findById = commonCacheKeyGenerator.generateKey(findByIdMethod, id);
            keys.add(findById);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
        }

        try {
            Method statusMethod = BlogWrapper.class.getMethod("findStatusById", Long.class);
            String status = commonCacheKeyGenerator.generateKey(statusMethod, id);
            keys.add(status);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
        }

        try {
            Method sensitiveMethod = BlogSensitiveWrapper.class.getMethod("findSensitiveByBlogId", Long.class);
            String sensitive = commonCacheKeyGenerator.generateKey(sensitiveMethod, id);
            keys.add(sensitive);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
        }

        keys.addAll(cacheKeyGenerator.generateHotBlogsKeys(year, count, countYear));
        cacheEvictHandler.evictCache(keys);
    }
}
