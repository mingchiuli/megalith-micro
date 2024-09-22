package org.chiu.micro.auth.dto;

import java.util.List;

public class AuthDto {

    private Long userId;

    private List<String> roles;

    private List<String> authorities;

    AuthDto(Long userId, List<String> roles, List<String> authorities) {
        this.userId = userId;
        this.roles = roles;
        this.authorities = authorities;
    }

    public static AuthDtoBuilder builder() {
        return new AuthDtoBuilder();
    }

    public Long getUserId() {
        return this.userId;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public List<String> getAuthorities() {
        return this.authorities;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthDto)) return false;
        final AuthDto other = (AuthDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$roles = this.getRoles();
        final Object other$roles = other.getRoles();
        if (this$roles == null ? other$roles != null : !this$roles.equals(other$roles)) return false;
        final Object this$authorities = this.getAuthorities();
        final Object other$authorities = other.getAuthorities();
        if (this$authorities == null ? other$authorities != null : !this$authorities.equals(other$authorities))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $roles = this.getRoles();
        result = result * PRIME + ($roles == null ? 43 : $roles.hashCode());
        final Object $authorities = this.getAuthorities();
        result = result * PRIME + ($authorities == null ? 43 : $authorities.hashCode());
        return result;
    }

    public String toString() {
        return "AuthDto(userId=" + this.getUserId() + ", roles=" + this.getRoles() + ", authorities=" + this.getAuthorities() + ")";
    }

    public static class AuthDtoBuilder {
        private Long userId;
        private List<String> roles;
        private List<String> authorities;

        AuthDtoBuilder() {
        }

        public AuthDtoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuthDtoBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public AuthDtoBuilder authorities(List<String> authorities) {
            this.authorities = authorities;
            return this;
        }

        public AuthDto build() {
            return new AuthDto(this.userId, this.roles, this.authorities);
        }

        public String toString() {
            return "AuthDto.AuthDtoBuilder(userId=" + this.userId + ", roles=" + this.roles + ", authorities=" + this.authorities + ")";
        }
    }
}
