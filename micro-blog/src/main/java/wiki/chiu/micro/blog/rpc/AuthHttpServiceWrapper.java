package wiki.chiu.micro.blog.rpc;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.api.AuthHttpService;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.blog.service.port.CollaborationTicketGateway;
import wiki.chiu.micro.common.rpc.RemoteResult;

@Component
public class AuthHttpServiceWrapper implements CollaborationTicketGateway {

  private final AuthHttpService authHttpService;

  public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
    this.authHttpService = authHttpService;
  }

  @Override
  public String issueTicket(Long userId, String roomId) {
    return RemoteResult.requireSuccess(
        () -> authHttpService.issueWebSocketTicket(new WebSocketTicketReq(userId, roomId)));
  }
}
