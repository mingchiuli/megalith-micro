package wiki.chiu.micro.exhibit.cache.handler;

import wiki.chiu.micro.cache.handler.CacheEvictHandler;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.utils.KeyUtils;
import wiki.chiu.micro.exhibit.cache.config.CacheKeyGenerator;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static wiki.chiu.micro.common.lang.Const.READ_TOKEN;
import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

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

        //保守处理，前面的全删
        long countAfter = blogHttpServiceWrapper.countByCreatedGreaterThanEqual(blogEntity.created());
        long countYearAfter = blogHttpServiceWrapper.getPageCountYear(blogEntity.created(), start, end);
        Set<String> keys = cacheKeyGenerator.generateBlogKey(countAfter, countYearAfter, year);

        log.info("keys:{}", keys);
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

        cacheEvictHandler.evictCache(keys);

        var clearKeys = new HashSet<String>();
        if (NORMAL.getCode().equals(status)) {
            clearKeys.add(READ_TOKEN + id);
        }
        //暂存区
        clearKeys.add(KeyUtils.createBlogEditRedisKey(blogEntity.userId(), id));
        redissonClient.getKeys().delete(clearKeys.toArray(new String[0]));
    }
}
