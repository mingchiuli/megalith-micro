package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.entity.UserEntity;

public class UserEntityRpcVoConvertor {

    private UserEntityRpcVoConvertor() {
    }

    public static UserEntityRpcVo convert(UserEntity user) {
        return UserEntityRpcVo.builder()
                .id(user.getId())
                .password(user.getPassword())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .created(user.getCreated())
                .updated(user.getUpdated())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
