package org.chiu.micro.user.listener;

import org.chiu.micro.user.config.UserAuthMenuChangeRabbitConfig;
import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.constant.UserAuthMenuOperateMessage;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthMenuOperateEventListener {

    private final RabbitTemplate rabbitTemplate;

    public AuthMenuOperateEventListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    public void process(AuthMenuOperateEvent event) {

        AuthMenuIndexMessage authMenuIndexMessage = event.getAuthMenuIndexMessage();
        List<String> roles = authMenuIndexMessage.roles();
        Integer type = authMenuIndexMessage.type();
        var data = UserAuthMenuOperateMessage.builder()
                .roles(roles)
                .type(type)
                .build();
        rabbitTemplate.convertAndSend(UserAuthMenuChangeRabbitConfig.FANOUT_EXCHANGE, "", data);
    }

}
