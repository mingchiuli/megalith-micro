package wiki.chiu.micro.auth.handler;

import static wiki.chiu.micro.common.auth.web.AuthWeb.authPrincipal;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.common.lang.Result;

@Component
public class AuthHttpHandler {

  private final AuthService authService;

  public AuthHttpHandler(AuthService authService) {
    this.authService = authService;
  }

  public ServerResponse nav(ServerRequest request) {
    var principal = authPrincipal(request);
    return ok(Result.success(() -> authService.getCurrentUserNav(principal.roles())));
  }
}
