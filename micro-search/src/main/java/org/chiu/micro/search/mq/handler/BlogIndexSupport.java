package org.chiu.micro.search.mq.handler;


import org.chiu.micro.search.dto.BlogEntityDto;
import org.chiu.micro.search.rpc.wrapper.BlogHttpServiceWrapper;

import java.util.Objects;

import org.chiu.micro.search.constant.BlogOperateEnum;
import org.chiu.micro.search.constant.BlogOperateMessage;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;

@Slf4j
public abstract sealed class BlogIndexSupport permits
        CreateBlogIndexHandler,
        RemoveBlogIndexHandler,
        UpdateBlogIndexHandler {

    protected final BlogHttpServiceWrapper blogHttpServiceWrapper;

    protected BlogIndexSupport(BlogHttpServiceWrapper blogHttpServiceWrapper) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
    }

    public abstract boolean supports(BlogOperateEnum blogOperateEnum);
    protected abstract void elasticSearchProcess(BlogEntityDto blog);

    @SneakyThrows
    public void handle(BlogOperateMessage message, Channel channel, Message msg) {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        try {
            Long blogId = message.getBlogId();
            BlogEntityDto blogEntity;
            if (Objects.equals(message.getTypeEnum(), BlogOperateEnum.REMOVE)) {
                blogEntity = new BlogEntityDto();
                blogEntity.setId(blogId);
            } else {
                blogEntity = blogHttpServiceWrapper.findById(blogId);
            }

            elasticSearchProcess(blogEntity);
            //手动签收消息
            //false代表不是批量签收模式
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("consume failure", e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

}
