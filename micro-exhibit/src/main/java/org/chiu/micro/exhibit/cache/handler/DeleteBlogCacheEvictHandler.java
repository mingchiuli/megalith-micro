package org.chiu.micro.exhibit.cache.handler;

import org.chiu.micro.cache.handler.CacheEvictHandler;
import org.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import org.chiu.micro.common.dto.BlogEntityRpcDto;
import org.chiu.micro.common.utils.KeyUtils;
import org.chiu.micro.exhibit.cache.config.CacheKeyGenerator;
import org.chiu.micro.exhibit.constant.BlogOperateEnum;
import org.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import org.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.chiu.micro.common.lang.Const.*;


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
            Method getCountByYearMethod  = BlogWrapper.class.getMethod("getCountByYear", Integer.class);
            String getCountByYear = commonCacheKeyGenerator.generateKey(getCountByYearMethod, year);
            keys.add(getCountByYear);
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
        //删掉所有摘要缓存
        var start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        var end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        long count = blogHttpServiceWrapper.count();
        long countYear = blogHttpServiceWrapper.countByCreatedBetween(start, end);
        keys.addAll(cacheKeyGenerator.generateHotBlogsKeys(year, count, countYear));

        keys.add(READ_TOKEN + id);

        String blogEditKey = KeyUtils.createBlogEditRedisKey(blogEntity.userId(), id);
        //删除该年份的页面bloom，listPage的bloom，getCountByYear的bloom，后面逻辑重建
        keys.add(BLOOM_FILTER_YEAR_PAGE + year);
        keys.add(BLOOM_FILTER_PAGE);
        keys.add(BLOOM_FILTER_YEARS);
        //暂存区
        keys.add(blogEditKey);
        //内容状态信息
        Set<String> localKeys = new HashSet<>(keys);
        localKeys.remove(BLOOM_FILTER_YEAR_PAGE + year);
        localKeys.remove(BLOOM_FILTER_PAGE);
        localKeys.remove(BLOOM_FILTER_YEARS);
        localKeys.remove(blogEditKey);
        localKeys.remove(READ_TOKEN + id);
        cacheEvictHandler.evictCache(keys, localKeys);

        //设置getBlogDetail的bloom
        redissonClient.getBitSet(BLOOM_FILTER_BLOG).set(id, false);
        //重置该年份的页面bloom
        int totalPageByPeriod = (int) (countYear % blogPageSize == 0 ? countYear / blogPageSize : countYear / blogPageSize + 1);
        for (int i = 1; i <= totalPageByPeriod; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_YEAR_PAGE + year).set(i, true);
        }

        //listPage的bloom
        int totalPage = (int) (count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1);
        for (int i = 1; i <= totalPage; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_PAGE).set(i, true);
        }

        //getCountByYear的bloom
        List<Integer> years = blogHttpServiceWrapper.getYears();
        years.forEach(y -> redissonClient.getBitSet(BLOOM_FILTER_YEARS).set(y, true));

        //删除最近热度
        redissonClient.getScoredSortedSet(HOT_READ).remove(id.toString());
    }
}
