package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.vo.UserInfoVo;
import org.chiu.micro.common.dto.UserEntityRpcDto;

public class UserInfoVoConvertor {

    private UserInfoVoConvertor() {}

    public static UserInfoVo convert(UserEntityRpcDto userEntity) {
        return UserInfoVo.builder()
                .avatar(userEntity.avatar())
                .nickname(userEntity.nickname())
                .build();
    }
}
