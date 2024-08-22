package org.chiu.micro.exhibit.cache.handler;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.chiu.micro.exhibit.config.CacheBlogEvictRabbitConfig;
import org.chiu.micro.exhibit.constant.BlogOperateEnum;
import org.chiu.micro.exhibit.constant.BlogOperateMessage;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Slf4j
public abstract sealed class BlogCacheEvictHandler permits
        CreateBlogCacheEvictHandler,
        DeleteBlogCacheEvictHandler,
        UpdateBlogCacheEvictHandler {

    protected final StringRedisTemplate redisTemplate;

    protected final BlogHttpServiceWrapper blogHttpServiceWrapper;

    protected final RabbitTemplate rabbitTemplate;

    protected BlogCacheEvictHandler(StringRedisTemplate redisTemplate,
                                    BlogHttpServiceWrapper blogHttpServiceWrapper,
                                    RabbitTemplate rabbitTemplate) {
        this.redisTemplate = redisTemplate;
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    public abstract boolean supports(BlogOperateEnum blogOperateEnum);

    protected abstract Set<String> redisProcess(BlogEntityDto blog);


    @SneakyThrows
    public void handle(BlogOperateMessage message, Channel channel, Message msg) {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        try {
            Long blogId = message.getBlogId();
            Integer year = message.getYear();
            BlogEntityDto blogEntity;
            if (Objects.equals(message.getTypeEnum(), BlogOperateEnum.REMOVE)) {
                blogEntity = BlogEntityDto.builder()
                        .id(blogId)
                        .created(LocalDateTime.of(year, 1, 1, 0, 0, 0))
                        .build();
            } else {
                blogEntity = blogHttpServiceWrapper.findById(blogId, year);
            }
            Set<String> keys = redisProcess(blogEntity);
            rabbitTemplate.convertAndSend(CacheBlogEvictRabbitConfig.CACHE_BLOG_EVICT_FANOUT_EXCHANGE, "", keys);

            //手动签收消息
            //false代表不是批量签收模式
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("consume failure", e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
