package org.chiu.micro.exhibit.cache.handler;

import lombok.SneakyThrows;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.chiu.micro.exhibit.cache.config.CacheKeyGenerator;
import org.chiu.micro.exhibit.constant.BlogOperateEnum;
import org.chiu.micro.exhibit.key.KeyFactory;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.chiu.micro.exhibit.lang.Const.*;

@Component
public final class DeleteBlogCacheEvictHandler extends BlogCacheEvictHandler {


    private final CacheKeyGenerator cacheKeyGenerator;

    @Value("${blog.blog-page-size}")
    private int blogPageSize;

    public DeleteBlogCacheEvictHandler(StringRedisTemplate redisTemplate,
                                       BlogHttpServiceWrapper blogHttpServiceWrapper,
                                       CacheKeyGenerator cacheKeyGenerator,
                                       RabbitTemplate rabbitTemplate) {
        super(redisTemplate, blogHttpServiceWrapper, rabbitTemplate);
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.REMOVE.equals(blogOperateEnum);
    }

    @SneakyThrows
    @Override
    public Set<String> redisProcess(BlogEntityDto blogEntity) {
        Long id = blogEntity.getId();
        int year = blogEntity.getCreated().getYear();

        //博客对象本身缓存
        Method findByIdMethod = BlogWrapper.class.getMethod("findById", Long.class);
        String findById = cacheKeyGenerator.generateKey(findByIdMethod, id);
        Method getCountByYearMethod = BlogWrapper.class.getMethod("getCountByYear", Integer.class);
        String getCountByYear = cacheKeyGenerator.generateKey(getCountByYearMethod, year);
        Method statusMethod = BlogWrapper.class.getMethod("findStatusById", Long.class);
        String status = cacheKeyGenerator.generateKey(statusMethod, id);
        Method sensitiveMethod = BlogSensitiveWrapper.class.getMethod("findSensitiveByBlogId", Long.class);
        String sensitive = cacheKeyGenerator.generateKey(sensitiveMethod, id);
        //删掉所有摘要缓存
        var start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        var end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        long count = blogHttpServiceWrapper.count();
        long countYear = blogHttpServiceWrapper.countByCreatedBetween(start, end);
        Set<String> keys = cacheKeyGenerator.generateHotBlogsKeys(year, count, countYear);

        keys.add(READ_TOKEN.getInfo() + id);
        keys.add(findById);
        keys.add(getCountByYear);
        keys.add(status);
        keys.add(sensitive);

        String blogEditKey = KeyFactory.createBlogEditRedisKey(blogEntity.getUserId(), id);
        //删除该年份的页面bloom，listPage的bloom，getCountByYear的bloom，后面逻辑重建
        keys.add(BLOOM_FILTER_YEAR_PAGE.getInfo() + year);
        keys.add(BLOOM_FILTER_PAGE.getInfo());
        keys.add(BLOOM_FILTER_YEARS.getInfo());
        //暂存区
        keys.add(blogEditKey);
        //内容状态信息
        redisTemplate.delete(keys);
        keys.remove(BLOOM_FILTER_YEAR_PAGE.getInfo() + year);
        keys.remove(BLOOM_FILTER_PAGE.getInfo());
        keys.remove(BLOOM_FILTER_YEARS.getInfo());
        keys.remove(blogEditKey);
        keys.remove(READ_TOKEN.getInfo() + id);

        //设置getBlogDetail的bloom
        redisTemplate.opsForValue().setBit(BLOOM_FILTER_BLOG.getInfo(), id, false);
        //重置该年份的页面bloom
        int totalPageByPeriod = (int) (countYear % blogPageSize == 0 ? countYear / blogPageSize : countYear / blogPageSize + 1);
        for (int i = 1; i <= totalPageByPeriod; i++) {
            redisTemplate.opsForValue().setBit(BLOOM_FILTER_YEAR_PAGE.getInfo() + year, i, true);
        }

        //listPage的bloom
        int totalPage = (int) (count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1);
        for (int i = 1; i <= totalPage; i++) {
            redisTemplate.opsForValue().setBit(BLOOM_FILTER_PAGE.getInfo(), i, true);
        }

        //getCountByYear的bloom
        List<Integer> years = blogHttpServiceWrapper.getYears();
        years.forEach(y -> redisTemplate.opsForValue().setBit(BLOOM_FILTER_YEARS.getInfo(), y, true));

        //删除最近热度
        redisTemplate.opsForZSet().remove(HOT_READ.getInfo(), id.toString());

        return keys;
    }
}
