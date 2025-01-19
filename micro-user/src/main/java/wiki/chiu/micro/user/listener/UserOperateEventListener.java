package wiki.chiu.micro.user.listener;

import wiki.chiu.micro.user.constant.UserIndexMessage;
import wiki.chiu.micro.user.event.UserOperateEvent;
import wiki.chiu.micro.user.lang.UserOperateEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static wiki.chiu.micro.common.lang.Const.BLOCK_USER;


@Component
public class UserOperateEventListener {

    private final StringRedisTemplate redisTemplate;

    @Value("${megalith.blog.jwt.access-token-expire}")
    private long accessExpire;

    public UserOperateEventListener(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @EventListener
    public void process(UserOperateEvent event) {
        UserIndexMessage userIndexMessage = event.getUserIndexMessage();
        Long userId = userIndexMessage.userId();
        UserOperateEnum userOperateEnum = userIndexMessage.userOperateEnum();

        if (UserOperateEnum.UPDATE.equals(userOperateEnum)) {
            redisTemplate.opsForValue().set(BLOCK_USER + userId, "", accessExpire, TimeUnit.SECONDS);
        }

        if (UserOperateEnum.DELETE.equals(userOperateEnum)) {
            redisTemplate.opsForValue().set(BLOCK_USER + userId, "", accessExpire, TimeUnit.SECONDS);
        }
    }
}
