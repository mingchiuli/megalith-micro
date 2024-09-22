package org.chiu.micro.auth.vo;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2023-04-15 2:14 pm
 */

public class UserInfoVo implements Serializable {

    private String nickname;

    private String avatar;

    UserInfoVo(String nickname, String avatar) {
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public static UserInfoVoBuilder builder() {
        return new UserInfoVoBuilder();
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserInfoVo)) return false;
        final UserInfoVo other = (UserInfoVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$nickname = this.getNickname();
        final Object other$nickname = other.getNickname();
        if (this$nickname == null ? other$nickname != null : !this$nickname.equals(other$nickname)) return false;
        final Object this$avatar = this.getAvatar();
        final Object other$avatar = other.getAvatar();
        if (this$avatar == null ? other$avatar != null : !this$avatar.equals(other$avatar)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserInfoVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $nickname = this.getNickname();
        result = result * PRIME + ($nickname == null ? 43 : $nickname.hashCode());
        final Object $avatar = this.getAvatar();
        result = result * PRIME + ($avatar == null ? 43 : $avatar.hashCode());
        return result;
    }

    public String toString() {
        return "UserInfoVo(nickname=" + this.getNickname() + ", avatar=" + this.getAvatar() + ")";
    }

    public static class UserInfoVoBuilder {
        private String nickname;
        private String avatar;

        UserInfoVoBuilder() {
        }

        public UserInfoVoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserInfoVoBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserInfoVo build() {
            return new UserInfoVo(this.nickname, this.avatar);
        }

        public String toString() {
            return "UserInfoVo.UserInfoVoBuilder(nickname=" + this.nickname + ", avatar=" + this.avatar + ")";
        }
    }
}
