package wiki.chiu.micro.common.auth.web;

import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.function.ServerRequest;
import wiki.chiu.micro.auth.api.AuthHttpService;
import wiki.chiu.micro.auth.api.vo.AuthRpcVo;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.common.security.AuthPrincipal;

public final class AuthWeb {

  private AuthWeb() {}

  public static AuthPrincipal authPrincipal(
      ServerRequest request, AuthHttpService authHttpService) {
    String token =
        Objects.requireNonNullElse(request.headers().firstHeader(HttpHeaders.AUTHORIZATION), "");
    AuthRpcVo auth = RemoteResult.requireSuccess(() -> authHttpService.getAuthentication(token));
    return AuthPrincipal.builder()
        .userId(auth.userId())
        .roles(auth.roles())
        .authorities(auth.authorities())
        .build();
  }
}
