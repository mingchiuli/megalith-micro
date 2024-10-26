package wiki.chiu.micro.user.constant;


import wiki.chiu.micro.user.lang.UserOperateEnum;

import java.io.Serializable;

public record UserIndexMessage(

        Long userId,

        UserOperateEnum userOperateEnum) implements Serializable {

}
