package wiki.chiu.micro.websocket.config.interceptor;

import wiki.chiu.micro.common.dto.AuthorityRouteRpcDto;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.websocket.rpc.AuthHttpServiceWrapper;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public AuthHandshakeInterceptor(AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        super.beforeHandshake(request, response, wsHandler, attributes);
        String query = request.getURI().getQuery();
        String token;

        try {
            token = query.substring("token=".length());
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        if (!StringUtils.hasLength(token)) {
            return false;
        }

        AuthorityRouteReq req = AuthorityRouteReq.builder()
                .ipAddr(null)
                .method(request.getMethod().name())
                .routeMapping(request.getURI().getPath())
                .build();
        AuthorityRouteRpcDto authorityRoute = authHttpServiceWrapper.getAuthorityRoute(req, token);
        return Boolean.TRUE.equals(authorityRoute.auth());
    }
}