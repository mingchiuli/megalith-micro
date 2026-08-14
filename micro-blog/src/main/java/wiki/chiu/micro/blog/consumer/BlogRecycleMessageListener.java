package wiki.chiu.micro.blog.consumer;

import static wiki.chiu.micro.common.lang.Const.A_WEEK;
import static wiki.chiu.micro.common.lang.Const.QUERY_DELETED;
import static wiki.chiu.micro.common.lang.Const.RECYCLE_EVENT_PREFIX;

import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.blog.convertor.BlogDeleteDtoConvertor;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

@Component
public class BlogRecycleMessageListener {

  private final StringRedisTemplate redis;
  private final ResourceLoader resources;
  private final JsonMapper jsonMapper;
  private final RetryingMessageRecoverer recoverer;
  private String recycleScript;

  public BlogRecycleMessageListener(
      StringRedisTemplate redis,
      ResourceLoader resources,
      JsonMapper jsonMapper,
      RetryingMessageRecoverer recoverer) {
    this.redis = redis;
    this.resources = resources;
    this.jsonMapper = jsonMapper;
    this.recoverer = recoverer;
  }

  @PostConstruct
  void loadScript() throws IOException {
    recycleScript =
        resources
            .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/blog-recycle.lua")
            .getContentAsString(StandardCharsets.UTF_8);
  }

  @RabbitListener(
      queues = Const.RECYCLE_QUEUE,
      concurrency = "2",
      messageConverter = "jsonMessageConverter",
      executor = "commonExecutor")
  public void handle(BlogChangedMessage event, Channel channel, Message message) {
    try {
      if (BlogOperateEnum.REMOVE.equals(BlogOperateEnum.of(event.operation()))) {
        if (event.operatorUserId() == null) {
          throw new IllegalArgumentException("Delete event is missing operatorUserId");
        }
        redis.execute(
            RedisScript.of(recycleScript, Long.class),
            List.of(QUERY_DELETED + event.operatorUserId(), RECYCLE_EVENT_PREFIX + event.eventId()),
            jsonMapper.writeValueAsString(BlogDeleteDtoConvertor.convert(event.blogSnapshot())),
            A_WEEK);
      }
      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception failure) {
      recoverer.recover(message, channel, failure);
    }
  }
}
