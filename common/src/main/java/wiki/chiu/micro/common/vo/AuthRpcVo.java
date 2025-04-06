package wiki.chiu.micro.common.vo;

import java.util.Collections;
import java.util.List;

public record AuthRpcVo(
        Long userId,
        List<String> roles,
        List<String> authorities) {

    public static AuthRpcVoBuilder builder() {
        return new AuthRpcVoBuilder(null, Collections.emptyList(), Collections.emptyList());
    }

    public record AuthRpcVoBuilder(
            Long userId,
            List<String> roles,
            List<String> authorities
    ) {

        public AuthRpcVoBuilder userId(Long userId) {
            return new AuthRpcVoBuilder(userId, this.roles, this.authorities);
        }

        public AuthRpcVoBuilder roles(List<String> roles) {
            return new AuthRpcVoBuilder(this.userId, roles, this.authorities);
        }

        public AuthRpcVoBuilder authorities(List<String> authorities) {
            return new AuthRpcVoBuilder(this.userId, this.roles, authorities);
        }

        public AuthRpcVo build() {
            return new AuthRpcVo(userId, roles, authorities);
        }
    }
}