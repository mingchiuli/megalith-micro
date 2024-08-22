package org.chiu.micro.exhibit.schedule.task;

import org.chiu.micro.exhibit.service.BlogService;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.chiu.micro.exhibit.lang.Const.BLOOM_FILTER_PAGE;

/**
 * @author mingchiuli
 * @create 2023-06-24 5:32 pm
 */
public record BlogsRunnable(
                            StringRedisTemplate redisTemplate,
                            BlogService blogService,
                            Integer pageNo) implements Runnable {

    @Override
    public void run() {
        redisTemplate.opsForValue().setBit(BLOOM_FILTER_PAGE.getInfo(), pageNo, true);
        blogService.findPage(pageNo, Integer.MIN_VALUE);
    }
}
