package wiki.chiu.micro.websocket.config.user;

import java.security.Principal;

public class AuthUser implements Principal {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
