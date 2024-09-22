package org.chiu.micro.auth.vo;


public record AuthorityRouteVo(
        Boolean auth,

        String serviceHost,

        Integer servicePort) {

    public static AuthorityRouteVoBuilder builder() {
        return new AuthorityRouteVoBuilder();
    }

    public static class AuthorityRouteVoBuilder {
        private Boolean auth;
        private String serviceHost;
        private Integer servicePort;

        public AuthorityRouteVoBuilder auth(Boolean auth) {
            this.auth = auth;
            return this;
        }

        public AuthorityRouteVoBuilder serviceHost(String serviceHost) {
            this.serviceHost = serviceHost;
            return this;
        }

        public AuthorityRouteVoBuilder servicePort(Integer servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public AuthorityRouteVo build() {
            return new AuthorityRouteVo(auth, serviceHost, servicePort);
        }
    }
}
