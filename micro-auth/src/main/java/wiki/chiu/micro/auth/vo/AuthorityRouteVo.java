package wiki.chiu.micro.auth.vo;


public record AuthorityRouteVo(

        String serviceHost,

        Integer servicePort) {

    public static AuthorityRouteVoBuilder builder() {
        return new AuthorityRouteVoBuilder();
    }

    public static class AuthorityRouteVoBuilder {
        private String serviceHost;
        private Integer servicePort;

        public AuthorityRouteVoBuilder serviceHost(String serviceHost) {
            this.serviceHost = serviceHost;
            return this;
        }

        public AuthorityRouteVoBuilder servicePort(Integer servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public AuthorityRouteVo build() {
            return new AuthorityRouteVo(serviceHost, servicePort);
        }
    }
}
