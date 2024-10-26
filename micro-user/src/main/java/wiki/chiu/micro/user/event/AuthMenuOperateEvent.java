package wiki.chiu.micro.user.event;


import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.springframework.context.ApplicationEvent;


public class AuthMenuOperateEvent extends ApplicationEvent {

    private final AuthMenuIndexMessage authMenuIndexMessage;

    public AuthMenuOperateEvent(Object source, AuthMenuIndexMessage authMenuIndexMessage) {
        super(source);
        this.authMenuIndexMessage = authMenuIndexMessage;
    }

    public AuthMenuIndexMessage getAuthMenuIndexMessage() {
        return this.authMenuIndexMessage;
    }

}
