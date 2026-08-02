package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.common.lang.Result;

import java.security.Principal;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

@Component
public class TokenHttpHandler {

    private final TokenService tokenService;

    public TokenHttpHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public ServerResponse refreshToken(ServerRequest request) {
        return ok(Result.success(() -> tokenService.refreshToken(authenticatedUserId(request))));
    }

    public ServerResponse userinfo(ServerRequest request) {
        return ok(Result.success(() -> tokenService.userinfo(authenticatedUserId(request))));
    }

    private Long authenticatedUserId(ServerRequest request) {
        return request.principal()
                .map(Principal::getName)
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalStateException("Authenticated principal is missing"));
    }
}
