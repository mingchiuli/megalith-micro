package wiki.chiu.micro.common.req;

public record AuthorityRouteReq(

        String method,

        String routeMapping,

        String ipAddr) {

    public static AuthorityRouteReqBuilder builder() {
        return new AuthorityRouteReqBuilder(null, null, null);
    }

    public record AuthorityRouteReqBuilder(
            String method,
            String routeMapping,
            String ipAddr
    ) {

        public AuthorityRouteReqBuilder method(String method) {
            return new AuthorityRouteReqBuilder(method, this.routeMapping, this.ipAddr);
        }

        public AuthorityRouteReqBuilder routeMapping(String routeMapping) {
            return new AuthorityRouteReqBuilder(this.method, routeMapping, this.ipAddr);
        }

        public AuthorityRouteReqBuilder ipAddr(String ipAddr) {
            return new AuthorityRouteReqBuilder(this.method, this.routeMapping, ipAddr);
        }

        public AuthorityRouteReq build() {
            return new AuthorityRouteReq(method, routeMapping, ipAddr);
        }
    }
}
