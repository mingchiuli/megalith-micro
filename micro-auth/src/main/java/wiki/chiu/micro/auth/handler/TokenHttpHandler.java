package wiki.chiu.micro.auth.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.auth.token.RefreshTokenCookieManager;
import wiki.chiu.micro.common.lang.Result;

import java.security.Principal;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

@Component
public class TokenHttpHandler {

    private final TokenService tokenService;

    private final RefreshTokenCookieManager refreshTokenCookieManager;

    public TokenHttpHandler(TokenService tokenService,
                            RefreshTokenCookieManager refreshTokenCookieManager) {
        this.tokenService = tokenService;
        this.refreshTokenCookieManager = refreshTokenCookieManager;
    }

    public ServerResponse refreshToken(ServerRequest request) {
        return ok(Result.success(() -> tokenService.refreshToken(authenticatedUserId(request))));
    }

    public ServerResponse userinfo(ServerRequest request) {
        return ok(Result.success(() -> tokenService.userinfo(authenticatedUserId(request))));
    }

    public ServerResponse logout(ServerRequest request) {
        return ServerResponse.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieManager.expire().toString())
                .body(Result.success());
    }

    private Long authenticatedUserId(ServerRequest request) {
        return request.principal()
                .map(Principal::getName)
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalStateException("Authenticated principal is missing"));
    }
}
