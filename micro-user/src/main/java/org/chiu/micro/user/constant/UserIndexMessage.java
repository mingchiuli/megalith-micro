package org.chiu.micro.user.constant;


import org.chiu.micro.user.lang.UserOperateEnum;

import java.io.Serializable;

public class UserIndexMessage implements Serializable {

    private Long userId;

    private UserOperateEnum userOperateEnum;

    public UserIndexMessage(Long userId, UserOperateEnum userOperateEnum) {
        this.userId = userId;
        this.userOperateEnum = userOperateEnum;
    }

    public Long getUserId() {
        return this.userId;
    }

    public UserOperateEnum getUserOperateEnum() {
        return this.userOperateEnum;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUserOperateEnum(UserOperateEnum userOperateEnum) {
        this.userOperateEnum = userOperateEnum;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserIndexMessage)) return false;
        final UserIndexMessage other = (UserIndexMessage) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$userOperateEnum = this.getUserOperateEnum();
        final Object other$userOperateEnum = other.getUserOperateEnum();
        if (this$userOperateEnum == null ? other$userOperateEnum != null : !this$userOperateEnum.equals(other$userOperateEnum))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserIndexMessage;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $userOperateEnum = this.getUserOperateEnum();
        result = result * PRIME + ($userOperateEnum == null ? 43 : $userOperateEnum.hashCode());
        return result;
    }

    public String toString() {
        return "UserIndexMessage(userId=" + this.getUserId() + ", userOperateEnum=" + this.getUserOperateEnum() + ")";
    }
}
