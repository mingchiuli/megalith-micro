package org.chiu.micro.user.constant;


import org.chiu.micro.user.lang.UserOperateEnum;

import java.io.Serializable;

public record UserIndexMessage(

        Long userId,

        UserOperateEnum userOperateEnum) implements Serializable {

}
