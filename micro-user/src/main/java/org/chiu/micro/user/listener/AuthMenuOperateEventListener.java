package org.chiu.micro.user.listener;

import java.util.List;

import org.chiu.micro.user.config.UserAuthMenuChangeRabbitConfig;
import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.constant.UserAuthMenuOperateMessage;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthMenuOperateEventListener {

    private final RabbitTemplate rabbitTemplate;

    @EventListener
    @Async("commonExecutor")
    public void process(AuthMenuOperateEvent event) {

        AuthMenuIndexMessage authMenuIndexMessage = event.getAuthMenuIndexMessage();
        List<String> roles = authMenuIndexMessage.getRoles();
        Integer type = authMenuIndexMessage.getType();
        var data = UserAuthMenuOperateMessage.builder()
                .roles(roles)
                .type(type)
                .build();
        rabbitTemplate.convertAndSend(UserAuthMenuChangeRabbitConfig.FANOUT_EXCHANGE, "", data);
    }

}
