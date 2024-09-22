package org.chiu.micro.auth.vo;


public class AuthorityRouteVo {

    private Boolean auth;

    private String serviceHost;

    private Integer servicePort;

    AuthorityRouteVo(Boolean auth, String serviceHost, Integer servicePort) {
        this.auth = auth;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
    }

    public static AuthorityRouteVoBuilder builder() {
        return new AuthorityRouteVoBuilder();
    }

    public Boolean getAuth() {
        return this.auth;
    }

    public String getServiceHost() {
        return this.serviceHost;
    }

    public Integer getServicePort() {
        return this.servicePort;
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    public void setServicePort(Integer servicePort) {
        this.servicePort = servicePort;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthorityRouteVo)) return false;
        final AuthorityRouteVo other = (AuthorityRouteVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$auth = this.getAuth();
        final Object other$auth = other.getAuth();
        if (this$auth == null ? other$auth != null : !this$auth.equals(other$auth)) return false;
        final Object this$serviceHost = this.getServiceHost();
        final Object other$serviceHost = other.getServiceHost();
        if (this$serviceHost == null ? other$serviceHost != null : !this$serviceHost.equals(other$serviceHost))
            return false;
        final Object this$servicePort = this.getServicePort();
        final Object other$servicePort = other.getServicePort();
        if (this$servicePort == null ? other$servicePort != null : !this$servicePort.equals(other$servicePort))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthorityRouteVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $auth = this.getAuth();
        result = result * PRIME + ($auth == null ? 43 : $auth.hashCode());
        final Object $serviceHost = this.getServiceHost();
        result = result * PRIME + ($serviceHost == null ? 43 : $serviceHost.hashCode());
        final Object $servicePort = this.getServicePort();
        result = result * PRIME + ($servicePort == null ? 43 : $servicePort.hashCode());
        return result;
    }

    public String toString() {
        return "AuthorityRouteVo(auth=" + this.getAuth() + ", serviceHost=" + this.getServiceHost() + ", servicePort=" + this.getServicePort() + ")";
    }

    public static class AuthorityRouteVoBuilder {
        private Boolean auth;
        private String serviceHost;
        private Integer servicePort;

        AuthorityRouteVoBuilder() {
        }

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
            return new AuthorityRouteVo(this.auth, this.serviceHost, this.servicePort);
        }

        public String toString() {
            return "AuthorityRouteVo.AuthorityRouteVoBuilder(auth=" + this.auth + ", serviceHost=" + this.serviceHost + ", servicePort=" + this.servicePort + ")";
        }
    }
}
