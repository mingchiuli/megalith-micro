package wiki.chiu.micro.common.security;

import java.util.List;

public record AuthPrincipal(Long userId, List<String> roles, List<String> authorities) {

  public static AuthPrincipalBuilder builder() {
    return new AuthPrincipalBuilder();
  }

  public static class AuthPrincipalBuilder {
    private Long userId;
    private List<String> roles;
    private List<String> authorities;

    public AuthPrincipalBuilder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public AuthPrincipalBuilder roles(List<String> roles) {
      this.roles = roles;
      return this;
    }

    public AuthPrincipalBuilder authorities(List<String> authorities) {
      this.authorities = authorities;
      return this;
    }

    public AuthPrincipal build() {
      return new AuthPrincipal(userId, roles, authorities);
    }
  }
}
