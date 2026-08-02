package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.common.lang.Result;

import java.security.Principal;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

@Component
public class AuthHttpHandler {

    private final AuthService authService;

    public AuthHttpHandler(AuthService authService) {
        this.authService = authService;
    }

    public ServerResponse nav(ServerRequest request) {
        Long userId = request.principal()
                .map(Principal::getName)
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalStateException("Authenticated principal is missing"));
        return ok(Result.success(() -> authService.getCurrentUserNav(userId)));
    }
}
