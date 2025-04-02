package wiki.chiu.micro.auth.convertor;

import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;

public class UserInfoVoConvertor {

    private UserInfoVoConvertor() {}

    public static UserInfoVo convert(UserEntityRpcVo userEntity) {
        return UserInfoVo.builder()
                .id(userEntity.id())
                .avatar(userEntity.avatar())
                .nickname(userEntity.nickname())
                .build();
    }
}
