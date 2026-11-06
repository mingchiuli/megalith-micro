package wiki.chiu.micro.auth.api.vo;

import wiki.chiu.micro.common.security.AuthPrincipal;

public record AuthorityRouteRpcVo(
    String serviceHost, Integer servicePort, AuthPrincipal principal, Long authorizationExpiresAt) {

    public AuthorityRouteRpcVo(String serviceHost, Integer servicePort, AuthPrincipal principal) {
        this(serviceHost, servicePort, principal, null);
    }

    public static AuthorityRouteRpcVoBuilder builder() {
        return new AuthorityRouteRpcVoBuilder();
    }

    public static class AuthorityRouteRpcVoBuilder {
        private String serviceHost;
        private Integer servicePort;
        private AuthPrincipal principal;
        private Long authorizationExpiresAt;

        public AuthorityRouteRpcVoBuilder authorizationExpiresAt(Long value) {
            this.authorizationExpiresAt = value;
            return this;
        }

        public AuthorityRouteRpcVoBuilder serviceHost(String serviceHost) {
            this.serviceHost = serviceHost;
            return this;
        }

        public AuthorityRouteRpcVoBuilder servicePort(Integer servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public AuthorityRouteRpcVoBuilder principal(AuthPrincipal principal) {
            this.principal = principal;
            return this;
        }

        public AuthorityRouteRpcVo build() {
            return new AuthorityRouteRpcVo(serviceHost, servicePort, principal, authorizationExpiresAt);
        }
    }
}
