package org.chiu.micro.auth.req;

public class AuthorityRouteReq {

    private String method;

    private String routeMapping;

    private String ipAddr;

    AuthorityRouteReq(String method, String routeMapping, String ipAddr) {
        this.method = method;
        this.routeMapping = routeMapping;
        this.ipAddr = ipAddr;
    }

    public static AuthorityRouteReqBuilder builder() {
        return new AuthorityRouteReqBuilder();
    }

    public String getMethod() {
        return this.method;
    }

    public String getRouteMapping() {
        return this.routeMapping;
    }

    public String getIpAddr() {
        return this.ipAddr;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRouteMapping(String routeMapping) {
        this.routeMapping = routeMapping;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthorityRouteReq)) return false;
        final AuthorityRouteReq other = (AuthorityRouteReq) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$method = this.getMethod();
        final Object other$method = other.getMethod();
        if (this$method == null ? other$method != null : !this$method.equals(other$method)) return false;
        final Object this$routeMapping = this.getRouteMapping();
        final Object other$routeMapping = other.getRouteMapping();
        if (this$routeMapping == null ? other$routeMapping != null : !this$routeMapping.equals(other$routeMapping))
            return false;
        final Object this$ipAddr = this.getIpAddr();
        final Object other$ipAddr = other.getIpAddr();
        if (this$ipAddr == null ? other$ipAddr != null : !this$ipAddr.equals(other$ipAddr)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthorityRouteReq;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $method = this.getMethod();
        result = result * PRIME + ($method == null ? 43 : $method.hashCode());
        final Object $routeMapping = this.getRouteMapping();
        result = result * PRIME + ($routeMapping == null ? 43 : $routeMapping.hashCode());
        final Object $ipAddr = this.getIpAddr();
        result = result * PRIME + ($ipAddr == null ? 43 : $ipAddr.hashCode());
        return result;
    }

    public String toString() {
        return "AuthorityRouteReq(method=" + this.getMethod() + ", routeMapping=" + this.getRouteMapping() + ", ipAddr=" + this.getIpAddr() + ")";
    }

    public static class AuthorityRouteReqBuilder {
        private String method;
        private String routeMapping;
        private String ipAddr;

        AuthorityRouteReqBuilder() {
        }

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
            return new AuthorityRouteReq(this.method, this.routeMapping, this.ipAddr);
        }

        public String toString() {
            return "AuthorityRouteReq.AuthorityRouteReqBuilder(method=" + this.method + ", routeMapping=" + this.routeMapping + ", ipAddr=" + this.ipAddr + ")";
        }
    }
}
