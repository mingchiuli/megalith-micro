package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.UserEntityDto;
import org.chiu.micro.auth.vo.UserInfoVo;

public class UserInfoVoConvertor {

    private UserInfoVoConvertor() {}

    public static UserInfoVo convert(UserEntityDto userEntity) {
        return UserInfoVo.builder()
                .avatar(userEntity.getAvatar())
                .nickname(userEntity.getNickname())
                .build();
    }
}
