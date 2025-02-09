package wiki.chiu.micro.search.mq.handler;


import com.rabbitmq.client.Channel;
import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogOperateMessage;
import wiki.chiu.micro.search.rpc.BlogHttpServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.Objects;

public abstract sealed class BlogIndexSupport permits
        CreateBlogIndexHandler,
        RemoveBlogIndexHandler,
        UpdateBlogIndexHandler {

    private static final Logger log = LoggerFactory.getLogger(BlogIndexSupport.class);
    protected final BlogHttpServiceWrapper blogHttpServiceWrapper;

    protected BlogIndexSupport(BlogHttpServiceWrapper blogHttpServiceWrapper) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
    }

    public abstract boolean supports(BlogOperateEnum blogOperateEnum);

    protected abstract void elasticSearchProcess(BlogEntityRpcDto blog);

    public void handle(BlogOperateMessage message, Channel channel, Message msg) {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        try {
            Long blogId = message.blogId();
            BlogEntityRpcDto blogEntity;
            if (Objects.equals(BlogOperateEnum.of(message.typeEnumCode()), BlogOperateEnum.REMOVE)) {
                blogEntity = new BlogEntityRpcDto(blogId, null, null, null, null, null, null, null, null, null);
            } else {
                blogEntity = blogHttpServiceWrapper.findById(blogId);
            }

            elasticSearchProcess(blogEntity);
            //手动签收消息
            //false代表不是批量签收模式
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("consume failure", e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
    }

}
