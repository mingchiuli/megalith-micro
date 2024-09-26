package org.chiu.micro.websocket.config.handler;

import org.chiu.micro.websocket.config.user.AuthUser;
import org.chiu.micro.websocket.dto.AuthDto;
import org.chiu.micro.websocket.rpc.wrapper.AuthHttpServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class RetrieveUserHandler extends DefaultHandshakeHandler {

    private static final Logger log = LoggerFactory.getLogger(RetrieveUserHandler.class);


    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public RetrieveUserHandler(AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }

    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {

        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        Object token1 = attributes.get("token");
        log.info("determineUser token1:{}", token1);
        log.info("determineUser patch:{}", request.getURI().getPath());

        log.info("determineUser:{}", token);


        if (!StringUtils.hasLength(token)) {
            return null;
        }

        AuthDto authDto = authHttpServiceWrapper.getAuthentication(token);

        AuthUser user = new AuthUser();
        user.setName(authDto.userId().toString());
        return user;
    }
}
