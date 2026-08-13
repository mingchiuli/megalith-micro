package wiki.chiu.micro.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.PostExchange;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.common.lang.Result;

public interface AuthHttpService {

  @PostExchange("/auth/route")
  Result<AuthorityRouteRpcVo> getAuthorityRoute(
      @RequestBody AuthorityRouteReq req,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @PostExchange("/token/websocket")
  Result<String> issueWebSocketTicket(@RequestBody WebSocketTicketReq req);
}
