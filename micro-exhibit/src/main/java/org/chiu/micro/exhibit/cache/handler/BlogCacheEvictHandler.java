package org.chiu.micro.exhibit.cache.handler;

import com.rabbitmq.client.Channel;
import org.chiu.micro.cache.handler.CacheEvictHandler;
import org.chiu.micro.common.dto.BlogEntityRpcDto;
import org.chiu.micro.common.exception.MissException;
import org.chiu.micro.exhibit.constant.BlogOperateEnum;
import org.chiu.micro.exhibit.constant.BlogOperateMessage;
import org.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract sealed class BlogCacheEvictHandler permits
        CreateBlogCacheEvictHandler,
        DeleteBlogCacheEvictHandler,
        UpdateBlogCacheEvictHandler {

    private static final Logger log = LoggerFactory.getLogger(BlogCacheEvictHandler.class);
    protected final RedissonClient redissonClient;

    protected final BlogHttpServiceWrapper blogHttpServiceWrapper;

    protected final CacheEvictHandler cacheEvictHandler;

    protected BlogCacheEvictHandler(RedissonClient redissonClient,
                                    BlogHttpServiceWrapper blogHttpServiceWrapper,
                                    CacheEvictHandler cacheEvictHandler) {
        this.redissonClient = redissonClient;
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.cacheEvictHandler = cacheEvictHandler;
    }

    public abstract boolean supports(BlogOperateEnum blogOperateEnum);

    protected abstract void redisProcess(BlogEntityRpcDto blog);


    public void handle(BlogOperateMessage message, Channel channel, Message msg) {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        try {
            Long blogId = message.blogId();
            Integer year = message.year();
            BlogEntityRpcDto blogEntity;
            if (Objects.equals(message.typeEnum(), BlogOperateEnum.REMOVE)) {
                blogEntity = BlogEntityRpcDto.builder()
                        .id(blogId)
                        .created(LocalDateTime.of(year, 1, 1, 0, 0, 0))
                        .build();
            } else {
                blogEntity = blogHttpServiceWrapper.findById(blogId, year);
            }
            redisProcess(blogEntity);

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
