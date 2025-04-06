package wiki.chiu.micro.common.vo;

import java.util.List;

public record AuthRpcVo(

        Long userId,

        List<String> roles,

        List<String> authorities) {

    public static AuthDtoBuilder builder() {
        return new AuthDtoBuilder();
    }

    public static class AuthDtoBuilder {
        private Long userId;
        private List<String> roles;
        private List<String> authorities;

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

        public AuthRpcVo build() {
            return new AuthRpcVo(userId, roles, authorities);
        }

    }
}
