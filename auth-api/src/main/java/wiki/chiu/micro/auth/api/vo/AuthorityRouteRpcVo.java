package wiki.chiu.micro.auth.api.vo;

import wiki.chiu.micro.common.security.AuthPrincipal;

public record AuthorityRouteRpcVo(
    String serviceHost, Integer servicePort, AuthPrincipal principal) {

  public static AuthorityRouteRpcVoBuilder builder() {
    return new AuthorityRouteRpcVoBuilder();
  }

  public static class AuthorityRouteRpcVoBuilder {
    private String serviceHost;
    private Integer servicePort;
    private AuthPrincipal principal;

    public AuthorityRouteRpcVoBuilder serviceHost(String serviceHost) {
      this.serviceHost = serviceHost;
      return this;
    }

    public AuthorityRouteRpcVoBuilder servicePort(Integer servicePort) {
      this.servicePort = servicePort;
      return this;
    }

    public AuthorityRouteRpcVoBuilder principal(AuthPrincipal principal) {
      this.principal = principal;
      return this;
    }

    public AuthorityRouteRpcVo build() {
      return new AuthorityRouteRpcVo(serviceHost, servicePort, principal);
    }
  }
}
