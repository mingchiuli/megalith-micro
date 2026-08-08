package wiki.chiu.micro.auth.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredHeader;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.api.AuthHttpService;
import wiki.chiu.micro.auth.api.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.auth.api.vo.AuthRpcVo;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.converter.AuthRequestConverter;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.common.lang.Result;

@Component
public class AuthInternalHttpHandler implements AuthHttpService {

  private final AuthService authService;
  private final JwtTokenService jwtTokenService;

  public AuthInternalHttpHandler(AuthService authService, JwtTokenService jwtTokenService) {
    this.authService = authService;
    this.jwtTokenService = jwtTokenService;
  }

  public ServerResponse getAuthentication(ServerRequest request) {
    return ok(getAuthentication(requiredHeader(request, HttpHeaders.AUTHORIZATION)));
  }

  public ServerResponse getAuthorityRoute(ServerRequest request) throws Exception {
    return ok(
        getAuthorityRoute(
            AuthRequestConverter.toAuthorityRouteReq(request),
            requiredHeader(request, HttpHeaders.AUTHORIZATION)));
  }

  public ServerResponse routeCheck(ServerRequest request) throws Exception {
    return ok(
        routeCheck(
            AuthRequestConverter.toAuthorityRouteCheckReq(request),
            requiredHeader(request, HttpHeaders.AUTHORIZATION)));
  }

  public ServerResponse issueWebSocketTicket(ServerRequest request) throws Exception {
    return ok(issueWebSocketTicket(AuthRequestConverter.toWebSocketTicketReq(request)));
  }

  @Override
  public Result<AuthRpcVo> getAuthentication(String token) {
    return Result.success(authService.getAuthVo(token));
  }

  @Override
  public Result<AuthorityRouteRpcVo> getAuthorityRoute(AuthorityRouteReq req, String token) {
    return Result.success(() -> authService.findRoute(req));
  }

  @Override
  public Result<Boolean> routeCheck(AuthorityRouteCheckReq req, String token) {
    return Result.success(() -> authService.routeCheck(req, token));
  }

  @Override
  public Result<String> issueWebSocketTicket(WebSocketTicketReq req) {
    return Result.success(
        "Bearer " + jwtTokenService.issueWebSocketToken(req.userId(), req.roomId()));
  }
}
