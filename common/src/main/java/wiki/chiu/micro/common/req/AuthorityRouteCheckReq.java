package wiki.chiu.micro.common.req;

public record AuthorityRouteCheckReq(
        String method,
        String routeMapping) {

    public static AuthorityRouteCheckReqBuilder builder() {
        return new AuthorityRouteCheckReqBuilder(null, null);
    }

    public record AuthorityRouteCheckReqBuilder(
            String method,
            String routeMapping
    ) {

        public AuthorityRouteCheckReqBuilder method(String method) {
            return new AuthorityRouteCheckReqBuilder(method, this.routeMapping);
        }

        public AuthorityRouteCheckReqBuilder routeMapping(String routeMapping) {
            return new AuthorityRouteCheckReqBuilder(this.method, routeMapping);
        }

        public AuthorityRouteCheckReq build() {
            return new AuthorityRouteCheckReq(method, routeMapping);
        }
    }
}