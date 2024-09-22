package org.chiu.micro.exhibit.cache.handler;

import org.chiu.micro.exhibit.cache.config.CacheKeyGenerator;
import org.chiu.micro.exhibit.constant.BlogOperateEnum;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.key.KeyFactory;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;

import static org.chiu.micro.exhibit.lang.Const.READ_TOKEN;
import static org.chiu.micro.exhibit.lang.StatusEnum.NORMAL;

@Component
public final class UpdateBlogCacheEvictHandler extends BlogCacheEvictHandler {


    private static final Logger log = LoggerFactory.getLogger(UpdateBlogCacheEvictHandler.class);
    private final CacheKeyGenerator cacheKeyGenerator;


    public UpdateBlogCacheEvictHandler(RedissonClient redissonClient,
                                       BlogHttpServiceWrapper blogHttpServiceWrapper,
                                       CacheKeyGenerator cacheKeyGenerator,
                                       RabbitTemplate rabbitTemplate) {
        super(redissonClient, blogHttpServiceWrapper, rabbitTemplate);
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.UPDATE.equals(blogOperateEnum);
    }

    @Override
    public Set<String> redisProcess(BlogEntityDto blogEntity) {
        Long id = blogEntity.getId();
        int year = blogEntity.getCreated().getYear();
        Integer status = blogEntity.getStatus();

        //不分年份的页数
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        long countAfter = blogHttpServiceWrapper.countByCreatedGreaterThanEqual(blogEntity.getCreated());
        long countYearAfter = blogHttpServiceWrapper.getPageCountYear(blogEntity.getCreated(), start, end);
        Set<String> keys = cacheKeyGenerator.generateBlogKey(countAfter, countYearAfter, year);

        //博客对象本身缓存
        try {
            Method findByIdAndVisibleMethod = BlogWrapper.class.getMethod("findById", Long.class);
            String findByIdAndVisible = cacheKeyGenerator.generateKey(findByIdAndVisibleMethod, id);
            keys.add(findByIdAndVisible);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
        }


        try {
            Method statusMethod = BlogWrapper.class.getMethod("findStatusById", Long.class);
            String statusKey = cacheKeyGenerator.generateKey(statusMethod, id);
            keys.add(statusKey);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
        }

        try {
            Method sensitiveMethod = BlogSensitiveWrapper.class.getMethod("findSensitiveByBlogId", Long.class);
            String sensitive = cacheKeyGenerator.generateKey(sensitiveMethod, id);
            keys.add(sensitive);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage());
        }


        if (NORMAL.getCode().equals(status)) {
            keys.add(READ_TOKEN.getInfo() + id);
        }

        String blogEditKey = KeyFactory.createBlogEditRedisKey(blogEntity.getUserId(), id);
        //暂存区
        keys.add(blogEditKey);
        //内容状态信息
        redissonClient.getKeys().delete(keys.toArray(new String[0]));
        if (NORMAL.getCode().equals(status)) {
            keys.remove(READ_TOKEN.getInfo() + id);
        }
        keys.remove(blogEditKey);

        return keys;
    }
}
