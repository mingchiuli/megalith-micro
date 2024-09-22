package org.chiu.micro.user.constant;

import java.io.Serializable;
import java.util.List;

public class AuthMenuIndexMessage implements Serializable {

    private List<String> roles;

    private Integer type;

    public AuthMenuIndexMessage(List<String> roles, Integer type) {
        this.roles = roles;
        this.type = type;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public Integer getType() {
        return this.type;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthMenuIndexMessage)) return false;
        final AuthMenuIndexMessage other = (AuthMenuIndexMessage) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$roles = this.getRoles();
        final Object other$roles = other.getRoles();
        if (this$roles == null ? other$roles != null : !this$roles.equals(other$roles)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthMenuIndexMessage;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $roles = this.getRoles();
        result = result * PRIME + ($roles == null ? 43 : $roles.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    public String toString() {
        return "AuthMenuIndexMessage(roles=" + this.getRoles() + ", type=" + this.getType() + ")";
    }
}
