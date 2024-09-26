package org.chiu.micro.websocket.config.interceptor;

import org.chiu.micro.websocket.dto.AuthorityRouteDto;
import org.chiu.micro.websocket.lang.Const;
import org.chiu.micro.websocket.req.AuthorityRouteReq;
import org.chiu.micro.websocket.rpc.wrapper.AuthHttpServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthHandshakeInterceptor.class);
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

        String path = request.getURI().getPath();

        AuthorityRouteReq req = AuthorityRouteReq.builder()
                .ipAddr(null)
                .method(Const.WS.getInfo())
                .routeMapping(path)
                .build();
        AuthorityRouteDto authorityRoute = authHttpServiceWrapper.getAuthorityRoute(req);
        log.info("beforeHandshake:{}", authorityRoute);
        return Boolean.TRUE.equals(authorityRoute.auth());
    }
}
