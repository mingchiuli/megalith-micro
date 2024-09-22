package org.chiu.micro.auth.vo;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2023-04-15 2:14 pm
 */

public record UserInfoVo(

        String nickname,

        String avatar) implements Serializable {

    public static UserInfoVoBuilder builder() {
        return new UserInfoVoBuilder();
    }

    public static class UserInfoVoBuilder {
        private String nickname;
        private String avatar;

        public UserInfoVoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserInfoVoBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserInfoVo build() {
            return new UserInfoVo(nickname, avatar);
        }

    }
}
