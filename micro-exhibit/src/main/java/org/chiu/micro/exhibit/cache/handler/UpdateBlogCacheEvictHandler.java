package org.chiu.micro.exhibit.cache.handler;

import lombok.SneakyThrows;

import org.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.cache.config.CacheKeyGenerator;
import org.chiu.micro.exhibit.constant.BlogOperateEnum;
import org.chiu.micro.exhibit.key.KeyFactory;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;

import static org.chiu.micro.exhibit.lang.Const.READ_TOKEN;
import static org.chiu.micro.exhibit.lang.StatusEnum.NORMAL;

@Component
public final class UpdateBlogCacheEvictHandler extends BlogCacheEvictHandler {


    private final CacheKeyGenerator cacheKeyGenerator;


    public UpdateBlogCacheEvictHandler(StringRedisTemplate redisTemplate,
                                       BlogHttpServiceWrapper blogHttpServiceWrapper,
                                       CacheKeyGenerator cacheKeyGenerator,
                                       RabbitTemplate rabbitTemplate) {
        super(redisTemplate, blogHttpServiceWrapper, rabbitTemplate);
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.UPDATE.equals(blogOperateEnum);
    }

    @SneakyThrows
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
        Method findByIdAndVisibleMethod = BlogWrapper.class.getMethod("findById", Long.class);
        String findByIdAndVisible = cacheKeyGenerator.generateKey(findByIdAndVisibleMethod, id);
        Method statusMethod = BlogWrapper.class.getMethod("findStatusById", Long.class);
        String statusKey = cacheKeyGenerator.generateKey(statusMethod, id);
        Method sensitiveMethod = BlogSensitiveWrapper.class.getMethod("findSensitiveByBlogId", Long.class);
        String sensitive = cacheKeyGenerator.generateKey(sensitiveMethod, id);

        keys.add(findByIdAndVisible);
        keys.add(statusKey);
        keys.add(sensitive);

        if (NORMAL.getCode().equals(status)) {
            keys.add(READ_TOKEN.getInfo() + id);
        }

        String blogEditKey = KeyFactory.createBlogEditRedisKey(blogEntity.getUserId(), id);
        //暂存区
        keys.add(blogEditKey);
        //内容状态信息
        redisTemplate.delete(keys);
        if (NORMAL.getCode().equals(status)) {
            keys.remove(READ_TOKEN.getInfo() + id);
        }
        keys.remove(blogEditKey);

        return keys;
    }
}
