package wiki.chiu.micro.blog.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static wiki.chiu.micro.common.lang.Const.A_WEEK;
import static wiki.chiu.micro.common.lang.Const.QUERY_DELETED;
import static wiki.chiu.micro.common.lang.Const.RECYCLE_EVENT_PREFIX;

import com.rabbitmq.client.Channel;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.messaging.RetryingMessageRecoverer;

class BlogRecycleMessageListenerTest {

  @Test
  void removeEventUsesItsEventIdAsTheIdempotencyKeyBeforeAck() throws Exception {
    StringRedisTemplate redis = org.mockito.Mockito.mock(StringRedisTemplate.class);
    RetryingMessageRecoverer recoverer = org.mockito.Mockito.mock(RetryingMessageRecoverer.class);
    BlogRecycleMessageListener listener =
        new BlogRecycleMessageListener(
            redis, new DefaultResourceLoader(), JsonMapper.builder().build(), recoverer);
    listener.loadScript();
    Channel channel = org.mockito.Mockito.mock(Channel.class);
    Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(11L).build();
    LocalDateTime now = LocalDateTime.now();
    BlogSnapshot snapshot =
        new BlogSnapshot(9L, 42L, "title", "description", "content", now, now, 0, null, 1L, 3L);
    BlogChangedMessage event =
        new BlogChangedMessage(
            "event-7", BlogOperateEnum.REMOVE.getCode(), 3L, 42L, snapshot, 0L, 0L);

    listener.handle(event, channel, message);

    verify(redis)
        .execute(
            any(RedisScript.class),
            eq(List.of(QUERY_DELETED + 42L, RECYCLE_EVENT_PREFIX + "event-7")),
            anyString(),
            eq(A_WEEK));
    verify(channel).basicAck(11L, false);
  }
}
