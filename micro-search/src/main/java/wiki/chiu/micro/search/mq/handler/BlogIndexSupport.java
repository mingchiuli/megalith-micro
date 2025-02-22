package wiki.chiu.micro.search.mq.handler;


import com.rabbitmq.client.Channel;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogOperateMessage;
import wiki.chiu.micro.search.rpc.BlogHttpServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.io.IOException;

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

    protected abstract void elasticSearchProcess(BlogEntityRpcVo blog);

    public void handle(BlogOperateMessage message, Channel channel, Message msg) {
        final long deliveryTag = msg.getMessageProperties().getDeliveryTag();

        try {
            processBlogMessage(message);
            acknowledgeMessage(channel, deliveryTag);
        } catch (Exception e) {
            log.error("Failed to consume message", e);
            handleFailure(channel, deliveryTag);
        }
    }

    private void processBlogMessage(BlogOperateMessage message) {
        BlogEntityRpcVo blogEntity = getBlogEntity(message);
        elasticSearchProcess(blogEntity);
    }

    private BlogEntityRpcVo getBlogEntity(BlogOperateMessage message) {
        return BlogOperateEnum.REMOVE.equals(BlogOperateEnum.of(message.typeEnumCode()))
                ? new BlogEntityRpcVo(message.blogId())
                : blogHttpServiceWrapper.findById(message.blogId());
    }

    private void acknowledgeMessage(Channel channel, long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    private void handleFailure(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException ex) {
            log.error("Failed to negative acknowledge message: {}", ex.getMessage());
        }
    }

}
