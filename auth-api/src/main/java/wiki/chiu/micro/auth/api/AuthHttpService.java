package wiki.chiu.micro.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import wiki.chiu.micro.auth.api.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.auth.api.vo.AuthRpcVo;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.common.lang.Result;

public interface AuthHttpService {

  @GetExchange("/auth")
  Result<AuthRpcVo> getAuthentication(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @PostExchange("/auth/route/check")
  Result<Boolean> routeCheck(
      @RequestBody AuthorityRouteCheckReq req,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @PostExchange("/auth/route")
  Result<AuthorityRouteRpcVo> getAuthorityRoute(
      @RequestBody AuthorityRouteReq req,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @PostExchange("/token/websocket")
  Result<String> issueWebSocketTicket(@RequestBody WebSocketTicketReq req);
}
