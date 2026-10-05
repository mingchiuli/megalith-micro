package wiki.chiu.micro.auth.adapter.in.http;

import org.springframework.web.servlet.function.ServerRequest;

import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.auth.application.model.RouteQuery;
import wiki.chiu.micro.common.web.ValidatedRequest;

public final class AuthRequestConverter {

    private static final ValidatedRequest v = new ValidatedRequest();

    private AuthRequestConverter() {
    }

    public static AuthorityRouteReq toAuthorityRouteReq(ServerRequest request) throws Exception {
        AuthorityRouteReq req = request.body(AuthorityRouteReq.class);

        v.notBlank(req.method(), "method");
        v.notBlank(req.routeMapping(), "routeMapping");

        return req;
    }

    public static WebSocketTicketReq toWebSocketTicketReq(ServerRequest request) throws Exception {
        WebSocketTicketReq req = request.body(WebSocketTicketReq.class);

        v.notBlank(req.roomId(), "roomId");

        return req;
    }

    public static RouteQuery toRouteQuery(AuthorityRouteReq req) {
        return new RouteQuery(req.method(), req.routeMapping(), req.ipAddr());
    }
}
