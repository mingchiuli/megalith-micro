package wiki.chiu.micro.common.rpc.config;

import java.util.ArrayList;
import java.util.List;

public record AuthInfo(

        Long userId,

        List<String> roles,

        List<String> authorities) {

    public static AuthInfoBuilder builder() {
        return new AuthInfoBuilder(null, new ArrayList<>(), new ArrayList<>());
    }

    public record AuthInfoBuilder(
            Long userId,
            List<String> roles,
            List<String> authorities
    ) {

        public AuthInfoBuilder userId(Long userId) {
            return new AuthInfoBuilder(userId, this.roles, this.authorities);
        }

        public AuthInfoBuilder roles(List<String> roles) {
            return new AuthInfoBuilder(this.userId, roles, this.authorities);
        }

        public AuthInfoBuilder authorities(List<String> authorities) {
            return new AuthInfoBuilder(this.userId, this.roles, authorities);
        }

        public AuthInfo build() {
            return new AuthInfo(userId, roles, authorities);
        }

    }
}

