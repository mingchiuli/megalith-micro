package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.UserEntity;
import org.chiu.micro.user.req.UserEntityReq;

public class UserEntityConvertor {
    public static void convert(UserEntityReq userEntityReq, UserEntity userEntity) {
        userEntity.setId(userEntityReq.id().orElse(null));
        userEntity.setUsername(userEntityReq.username());
        userEntity.setNickname(userEntityReq.nickname());
        userEntity.setAvatar(userEntityReq.avatar());
        userEntity.setEmail(userEntityReq.email());
        userEntity.setPhone(userEntityReq.phone());
        userEntity.setPassword(userEntityReq.password());
        userEntity.setStatus(userEntityReq.status());
    }
}
