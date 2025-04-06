package wiki.chiu.micro.common.vo;


public record AuthorityRouteRpcVo(

        String serviceHost,

        Integer servicePort) {

    public static AuthorityRouteRpcVoBuilder builder() {
        return new AuthorityRouteRpcVoBuilder();
    }

    public static class AuthorityRouteRpcVoBuilder {
        private String serviceHost;
        private Integer servicePort;

        public AuthorityRouteRpcVoBuilder serviceHost(String serviceHost) {
            this.serviceHost = serviceHost;
            return this;
        }

        public AuthorityRouteRpcVoBuilder servicePort(Integer servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public AuthorityRouteRpcVo build() {
            return new AuthorityRouteRpcVo(serviceHost, servicePort);
        }
    }
}
