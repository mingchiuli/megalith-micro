package org.chiu.micro.user.event;

import org.chiu.micro.user.constant.UserIndexMessage;
import org.springframework.context.ApplicationEvent;

public class UserOperateEvent extends ApplicationEvent {

    private UserIndexMessage userIndexMessage;

    public UserOperateEvent(Object source, UserIndexMessage userIndexMessage) {
        super(source);
        this.userIndexMessage = userIndexMessage;
    }

    public UserIndexMessage getUserIndexMessage() {
        return this.userIndexMessage;
    }

    public void setUserIndexMessage(UserIndexMessage userIndexMessage) {
        this.userIndexMessage = userIndexMessage;
    }
}
