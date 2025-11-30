package wiki.chiu.micro.user.constant;

import java.util.List;

public record AuthMenuIndexMessage(

        List<String> roles,

        Integer type) {
}
