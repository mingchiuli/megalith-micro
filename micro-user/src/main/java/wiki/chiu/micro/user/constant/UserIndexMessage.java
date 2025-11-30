package wiki.chiu.micro.user.constant;


import wiki.chiu.micro.user.lang.UserOperateEnum;


public record UserIndexMessage(

        Long userId,

        UserOperateEnum userOperateEnum) {

}
