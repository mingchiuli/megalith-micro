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
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.chiu.micro.common.lang.Const.READ_TOKEN;
import static org.chiu.micro.common.lang.StatusEnum.NORMAL;

@Component
public final class UpdateBlogCacheEvictHandler extends BlogCacheEvictHandler {


    private static final Logger log = LoggerFactory.getLogger(UpdateBlogCacheEvictHandler.class);
    private final CacheKeyGenerator cacheKeyGenerator;

    private final CommonCacheKeyGenerator commonCacheKeyGenerator;


    public UpdateBlogCacheEvictHandler(RedissonClient redissonClient,
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
        return BlogOperateEnum.UPDATE.equals(blogOperateEnum);
    }

    @Override
    public void redisProcess(BlogEntityRpcDto blogEntity) {
        Long id = blogEntity.id();
        int year = blogEntity.created().getYear();
        Integer status = blogEntity.status();

        //不分年份的页数
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        long countAfter = blogHttpServiceWrapper.countByCreatedGreaterThanEqual(blogEntity.created());
        long countYearAfter = blogHttpServiceWrapper.getPageCountYear(blogEntity.created(), start, end);
        Set<String> keys = cacheKeyGenerator.generateBlogKey(countAfter, countYearAfter, year);

        //博客对象本身缓存
        try {
            Method findByIdAndVisibleMethod = BlogWrapper.class.getMethod("findById", Long.class);
            String findByIdAndVisible = commonCacheKeyGenerator.generateKey(findByIdAndVisibleMethod, id);
            keys.add(findByIdAndVisible);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
        }


        try {
            Method statusMethod = BlogWrapper.class.getMethod("findStatusById", Long.class);
            String statusKey = commonCacheKeyGenerator.generateKey(statusMethod, id);
            keys.add(statusKey);
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


        if (NORMAL.getCode().equals(status)) {
            keys.add(READ_TOKEN + id);
        }

        String blogEditKey = KeyUtils.createBlogEditRedisKey(blogEntity.userId(), id);
        //暂存区
        keys.add(blogEditKey);
        //内容状态信息
        redissonClient.getKeys().delete(keys.toArray(new String[0]));
        Set<String> localKeys = new HashSet<>(keys);
        if (NORMAL.getCode().equals(status)) {
            localKeys.remove(READ_TOKEN + id);
        }
        localKeys.remove(blogEditKey);
        cacheEvictHandler.evictCache(keys, localKeys);
    }
}
