package org.chiu.micro.user.listener;

import lombok.RequiredArgsConstructor;

import org.chiu.micro.user.constant.UserIndexMessage;
import org.chiu.micro.user.event.UserOperateEvent;
import org.chiu.micro.user.lang.UserOperateEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static org.chiu.micro.user.lang.Const.BLOCK_USER;


@Component
@RequiredArgsConstructor
public class UserOperateEventListener {

    private final StringRedisTemplate redisTemplate;

    @Value("${blog.jwt.access-token-expire}")
    private long accessExpire;

    @EventListener
    @Async("commonExecutor")
    public void process(UserOperateEvent event) {
        UserIndexMessage userIndexMessage = event.getUserIndexMessage();
        Long userId = userIndexMessage.getUserId();
        UserOperateEnum userOperateEnum = userIndexMessage.getUserOperateEnum();

        if (UserOperateEnum.UPDATE.equals(userOperateEnum)) {
            redisTemplate.opsForValue().set(BLOCK_USER.getInfo() + userId, "", accessExpire, TimeUnit.SECONDS);
        }
    }
}
