package wiki.chiu.micro.common.req;

public record AuthorityRouteCheckReq(

        String method,

        String routeMapping) {

    public static AuthorityRouteReqBuilder builder() {
        return new AuthorityRouteReqBuilder();
    }

    public static class AuthorityRouteReqBuilder {
        private String method;
        private String routeMapping;


        public AuthorityRouteReqBuilder method(String method) {
            this.method = method;
            return this;
        }

        public AuthorityRouteReqBuilder routeMapping(String routeMapping) {
            this.routeMapping = routeMapping;
            return this;
        }

        public AuthorityRouteCheckReq build() {
            return new AuthorityRouteCheckReq(method, routeMapping);
        }
    }
}
