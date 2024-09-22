package org.chiu.micro.gateway.dto;


public class AuthorityRouteDto {

    private Boolean auth;

    private String serviceHost;

    private Integer servicePort;

    public AuthorityRouteDto() {
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
        if (!(o instanceof AuthorityRouteDto)) return false;
        final AuthorityRouteDto other = (AuthorityRouteDto) o;
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
        return other instanceof AuthorityRouteDto;
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
        return "AuthorityRouteDto(auth=" + this.getAuth() + ", serviceHost=" + this.getServiceHost() + ", servicePort=" + this.getServicePort() + ")";
    }
}
