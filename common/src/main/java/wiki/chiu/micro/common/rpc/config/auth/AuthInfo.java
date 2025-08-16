package wiki.chiu.micro.common.rpc.config.auth;

import java.util.List;

public record AuthInfo(

        Long userId,

        List<String> roles,

        List<String> authorities) {

    public static AuthInfoBuilder builder() {
        return new AuthInfoBuilder();
    }

    public static class AuthInfoBuilder {
        private Long userId;
        private List<String> roles;
        private List<String> authorities;

        public AuthInfoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuthInfoBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public AuthInfoBuilder authorities(List<String> authorities) {
            this.authorities = authorities;
            return this;
        }

        public AuthInfo build() {
            return new AuthInfo(userId, roles, authorities);
        }

    }
}

