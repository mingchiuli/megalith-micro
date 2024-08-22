package org.chiu.micro.user.event;

import lombok.Getter;
import lombok.Setter;

import org.chiu.micro.user.constant.UserIndexMessage;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class UserOperateEvent extends ApplicationEvent {

    private UserIndexMessage userIndexMessage;

    public UserOperateEvent(Object source, UserIndexMessage userIndexMessage) {
        super(source);
        this.userIndexMessage = userIndexMessage;
    }
}
