package org.chiu.micro.exhibit.cache.handler;

import org.chiu.micro.exhibit.cache.config.CacheKeyGenerator;
import org.chiu.micro.exhibit.constant.BlogOperateEnum;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.key.KeyFactory;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

import static org.chiu.micro.exhibit.lang.Const.*;

@Component
public final class CreateBlogCacheEvictHandler extends BlogCacheEvictHandler {

    private final CacheKeyGenerator cacheKeyGenerator;

    @Value("${blog.blog-page-size}")
    private int blogPageSize;

    public CreateBlogCacheEvictHandler(RedissonClient redissonClient,
                                       BlogHttpServiceWrapper blogHttpServiceWrapper,
                                       CacheKeyGenerator cacheKeyGenerator,
                                       RabbitTemplate rabbitTemplate) {
        super(redissonClient, blogHttpServiceWrapper, rabbitTemplate);
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.CREATE.equals(blogOperateEnum);
    }

    @Override
    public Set<String> redisProcess(BlogEntityDto blogEntity) {
        Long id = blogEntity.id();
        int year = blogEntity.created().getYear();
        //删除listPageByYear、listPage、getCountByYear所有缓存，该年份的页面bloom，编辑暂存区数据
        var start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        var end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        long count = blogHttpServiceWrapper.count();
        long countYear = blogHttpServiceWrapper.countByCreatedBetween(start, end);
        Set<String> keys = cacheKeyGenerator.generateHotBlogsKeys(year, count, countYear);
        String blogEditKey = KeyFactory.createBlogEditRedisKey(blogEntity.userId(), null);
        keys.add(blogEditKey);
        redissonClient.getKeys().delete(keys.toArray(new String[0]));
        keys.remove(blogEditKey);

        //重新构建该年份的页面bloom
        int totalPageByPeriod = (int) (countYear % blogPageSize == 0 ? countYear / blogPageSize : countYear / blogPageSize + 1);
        for (int i = 1; i <= totalPageByPeriod; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_YEAR_PAGE.getInfo() + year).set(i, true);
        }

        //listPage的bloom
        int totalPage = (int) (count % blogPageSize == 0 ? count / blogPageSize : count / blogPageSize + 1);
        for (int i = 1; i <= totalPage; i++) {
            redissonClient.getBitSet(BLOOM_FILTER_PAGE.getInfo()).set(i, true);
        }

        //设置getBlogDetail的bloom, getBlogStatus的bloom(其实同一个bloom)和最近阅读数
        redissonClient.getBitSet(BLOOM_FILTER_BLOG.getInfo()).set(id, true);

        //年份过滤bloom更新,getCountByYear的bloom
        redissonClient.getBitSet(BLOOM_FILTER_YEARS.getInfo()).set(year, true);
        return keys;
    }
}
