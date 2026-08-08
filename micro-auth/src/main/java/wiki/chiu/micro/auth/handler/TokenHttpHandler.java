package wiki.chiu.micro.auth.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.auth.token.AccessTokenCookieManager;
import wiki.chiu.micro.auth.token.RefreshTokenCookieManager;
import wiki.chiu.micro.common.lang.Result;

@Component
public class TokenHttpHandler {

  private final TokenService tokenService;

  private final RefreshTokenCookieManager refreshTokenCookieManager;

  private final AccessTokenCookieManager accessTokenCookieManager;

  public TokenHttpHandler(
      TokenService tokenService,
      RefreshTokenCookieManager refreshTokenCookieManager,
      AccessTokenCookieManager accessTokenCookieManager) {
    this.tokenService = tokenService;
    this.refreshTokenCookieManager = refreshTokenCookieManager;
    this.accessTokenCookieManager = accessTokenCookieManager;
  }

  public ServerResponse refreshToken(ServerRequest request) {
    Map<String, String> token = tokenService.refreshToken(authenticatedUserId(request));
    return ServerResponse.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            accessTokenCookieManager.create(token.get("accessToken")).toString())
        .body(Result.success(token));
  }

  public ServerResponse userinfo(ServerRequest request) {
    return ok(Result.success(() -> tokenService.userinfo(authenticatedUserId(request))));
  }

  public ServerResponse logout(ServerRequest request) {
    return ServerResponse.ok()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookieManager.expire().toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookieManager.expire().toString())
        .body(Result.success());
  }

  private Long authenticatedUserId(ServerRequest request) {
    return request
        .principal()
        .map(Principal::getName)
        .map(Long::valueOf)
        .orElseThrow(() -> new IllegalStateException("Authenticated principal is missing"));
  }
}
