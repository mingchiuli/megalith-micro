package org.chiu.micro.common.req;

public record AuthorityRouteReq(

        String method,

        String routeMapping,

        String ipAddr) {

    public static AuthorityRouteReqBuilder builder() {
        return new AuthorityRouteReqBuilder();
    }

    public static class AuthorityRouteReqBuilder {
        private String method;
        private String routeMapping;
        private String ipAddr;


        public AuthorityRouteReqBuilder method(String method) {
            this.method = method;
            return this;
        }

        public AuthorityRouteReqBuilder routeMapping(String routeMapping) {
            this.routeMapping = routeMapping;
            return this;
        }

        public AuthorityRouteReqBuilder ipAddr(String ipAddr) {
            this.ipAddr = ipAddr;
            return this;
        }

        public AuthorityRouteReq build() {
            return new AuthorityRouteReq(method, routeMapping, ipAddr);
        }
    }
}
