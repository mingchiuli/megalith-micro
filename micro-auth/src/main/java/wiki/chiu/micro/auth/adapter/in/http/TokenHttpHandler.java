package wiki.chiu.micro.auth.adapter.in.http;

import static wiki.chiu.micro.common.auth.web.AuthWeb.authPrincipal;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.auth.application.port.in.TokenService;
import wiki.chiu.micro.common.result.Result;

@Component
public class TokenHttpHandler {

    private final TokenService tokenService;

    private final SessionCookies sessionCookies;

    public TokenHttpHandler(TokenService tokenService, SessionCookies sessionCookies) {
        this.tokenService = tokenService;
        this.sessionCookies = sessionCookies;
    }

    public ServerResponse refreshToken(ServerRequest request) {
        String accessToken = tokenService.refreshAccessToken(authenticatedUserId(request));
        return ServerResponse.ok()
            .header(HttpHeaders.SET_COOKIE, sessionCookies.access(accessToken).toString())
            .body(Result.success());
    }

    public ServerResponse userinfo(ServerRequest request) {
        return ok(
            Result.success(
                () -> AuthResponseMapper.toVo(tokenService.userinfo(authPrincipal(request).userId()))));
    }

    public ServerResponse logout(ServerRequest request) {
        return ServerResponse.ok()
            .header(HttpHeaders.SET_COOKIE, sessionCookies.expiredAccess().toString())
            .header(HttpHeaders.SET_COOKIE, sessionCookies.expiredRefresh().toString())
            .body(Result.success());
    }

    private Long authenticatedUserId(ServerRequest request) {
        return request
            .principal()
            .map(java.security.Principal::getName)
            .map(Long::valueOf)
            .orElseThrow(() -> new IllegalStateException("Authenticated principal is missing"));
    }
}
