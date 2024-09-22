package org.chiu.micro.user.constant;

import java.io.Serializable;
import java.util.List;

public record AuthMenuIndexMessage(

        List<String> roles,

        Integer type) implements Serializable {
}
