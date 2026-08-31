package wiki.chiu.micro.common.security;

import java.util.List;

import wiki.chiu.micro.common.lang.DataPermissionEnum;

public record AuthPrincipal(
    Long userId, List<String> roles, List<DataPermissionEnum> dataPermissions) {

    public AuthPrincipal {
        dataPermissions = dataPermissions == null ? List.of() : List.copyOf(dataPermissions);
    }

    public AuthPrincipal(Long userId, List<String> roles) {
        this(userId, roles, List.of());
    }

    public static AuthPrincipal anonymous() {
        return new AuthPrincipal(0L, List.of(), List.of());
    }

    public static AuthPrincipalBuilder builder() {
        return new AuthPrincipalBuilder();
    }

    public static class AuthPrincipalBuilder {
        private Long userId;
        private List<String> roles;
        private List<DataPermissionEnum> dataPermissions = List.of();

        public AuthPrincipalBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuthPrincipalBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public AuthPrincipalBuilder dataPermissions(List<DataPermissionEnum> dataPermissions) {
            this.dataPermissions = dataPermissions;
            return this;
        }

        public AuthPrincipal build() {
            return new AuthPrincipal(userId, roles, dataPermissions);
        }
    }
}
