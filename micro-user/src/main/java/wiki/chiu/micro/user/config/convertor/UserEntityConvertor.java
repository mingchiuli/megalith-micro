package wiki.chiu.micro.user.config.convertor;

import wiki.chiu.micro.user.domain.UserEntity;
import wiki.chiu.micro.user.req.UserEntityReq;

public class UserEntityConvertor {
  public static UserEntity convert(UserEntityReq userEntityReq, UserEntity dealUser) {
    UserEntity userEntity = new UserEntity();
    userEntity.setId(userEntityReq.id().orElse(null));
    userEntity.setUsername(userEntityReq.username());
    userEntity.setNickname(userEntityReq.nickname());
    userEntity.setAvatar(userEntityReq.avatar());
    userEntity.setEmail(userEntityReq.email());
    userEntity.setPhone(userEntityReq.phone());
    userEntity.setPassword(userEntityReq.password());
    userEntity.setStatus(userEntityReq.status());
    userEntity.setPasswordLockedUntil(null);

    userEntity.setCreated(dealUser.getCreated());
    userEntity.setLastLogin(dealUser.getLastLogin());

    return userEntity;
  }
}
