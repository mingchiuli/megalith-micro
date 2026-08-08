package wiki.chiu.micro.auth.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

import java.security.Principal;
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
    Long userId =
        request
            .principal()
            .map(Principal::getName)
            .map(Long::valueOf)
            .orElseThrow(() -> new IllegalStateException("Authenticated principal is missing"));
    return ok(Result.success(() -> authService.getCurrentUserNav(userId)));
  }
}
