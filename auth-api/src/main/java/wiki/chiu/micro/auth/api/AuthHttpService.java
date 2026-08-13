package wiki.chiu.micro.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.PostExchange;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.security.InternalHttpHeaders;

public interface AuthHttpService {

  @PostExchange(AuthHttpPaths.AUTH_ROUTE)
  Result<AuthorityRouteRpcVo> getAuthorityRoute(
      @RequestBody AuthorityRouteReq req,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @PostExchange(AuthHttpPaths.WEBSOCKET_TOKEN)
  Result<String> issueWebSocketTicket(
      @RequestBody WebSocketTicketReq req,
      @RequestHeader(value = InternalHttpHeaders.PRINCIPAL, required = false)
          String encodedPrincipal);

  default Result<String> issueWebSocketTicket(WebSocketTicketReq req) {
    return issueWebSocketTicket(req, null);
  }
}
