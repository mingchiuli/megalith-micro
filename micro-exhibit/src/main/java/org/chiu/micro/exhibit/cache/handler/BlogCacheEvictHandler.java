package org.chiu.micro.exhibit.cache.handler;

import com.rabbitmq.client.Channel;
import org.chiu.micro.exhibit.config.CacheBlogEvictRabbitConfig;
import org.chiu.micro.exhibit.constant.BlogOperateEnum;
import org.chiu.micro.exhibit.constant.BlogOperateMessage;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.exception.MissException;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

public abstract sealed class BlogCacheEvictHandler permits
        CreateBlogCacheEvictHandler,
        DeleteBlogCacheEvictHandler,
        UpdateBlogCacheEvictHandler {

    private static final Logger log = LoggerFactory.getLogger(BlogCacheEvictHandler.class);
    protected final RedissonClient redissonClient;

    protected final BlogHttpServiceWrapper blogHttpServiceWrapper;

    protected final RabbitTemplate rabbitTemplate;

    protected BlogCacheEvictHandler(RedissonClient redissonClient,
                                    BlogHttpServiceWrapper blogHttpServiceWrapper,
                                    RabbitTemplate rabbitTemplate) {
        this.redissonClient = redissonClient;
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    public abstract boolean supports(BlogOperateEnum blogOperateEnum);

    protected abstract Set<String> redisProcess(BlogEntityDto blog);


    public void handle(BlogOperateMessage message, Channel channel, Message msg) {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        try {
            Long blogId = message.blogId();
            Integer year = message.year();
            BlogEntityDto blogEntity;
            if (Objects.equals(message.typeEnum(), BlogOperateEnum.REMOVE)) {
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
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                throw new MissException(ex.getMessage());
            }
        }
    }
}
