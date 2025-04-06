package wiki.chiu.micro.common.vo;


public record AuthorityRouteRpcVo(

        String serviceHost,

        Integer servicePort) {

    public static AuthorityRouteRpcVoBuilder builder() {
        return new AuthorityRouteRpcVoBuilder(null, null);
    }

    public record AuthorityRouteRpcVoBuilder(
            String serviceHost,
            Integer servicePort
    ) {

        public AuthorityRouteRpcVoBuilder serviceHost(String serviceHost) {
            return new AuthorityRouteRpcVoBuilder(serviceHost, this.servicePort);
        }

        public AuthorityRouteRpcVoBuilder servicePort(Integer servicePort) {
            return new AuthorityRouteRpcVoBuilder(this.serviceHost, servicePort);
        }

        public AuthorityRouteRpcVo build() {
            return new AuthorityRouteRpcVo(serviceHost, servicePort);
        }
    }
}