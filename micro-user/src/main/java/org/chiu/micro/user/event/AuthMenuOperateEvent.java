package org.chiu.micro.user.event;


import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.springframework.context.ApplicationEvent;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthMenuOperateEvent extends ApplicationEvent {

    private AuthMenuIndexMessage authMenuIndexMessage;

    public AuthMenuOperateEvent(Object source, AuthMenuIndexMessage authMenuIndexMessage) {
        super(source);
        this.authMenuIndexMessage = authMenuIndexMessage;
    }
  
}
