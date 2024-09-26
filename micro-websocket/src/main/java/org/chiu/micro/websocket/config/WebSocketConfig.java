package org.chiu.micro.websocket.config;

import org.chiu.micro.websocket.config.handler.EditWebSocketHandler;
import org.chiu.micro.websocket.config.handler.RetrieveUserHandler;
import org.chiu.micro.websocket.config.interceptor.AuthHandshakeInterceptor;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.socket.config.annotation.*;


/**
 * @author mingchiuli
 * @create 2021-12-21 11:11 AM
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final EditWebSocketHandler editWebSocketHandler;

    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    private final RetrieveUserHandler retrieveUserHandler;

    public WebSocketConfig(EditWebSocketHandler editWebSocketHandler, AuthHandshakeInterceptor authHandshakeInterceptor, RetrieveUserHandler retrieveUserHandler) {
        this.editWebSocketHandler = editWebSocketHandler;
        this.authHandshakeInterceptor = authHandshakeInterceptor;
        this.retrieveUserHandler = retrieveUserHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(editWebSocketHandler, "/edit/ws")
                .setHandshakeHandler(retrieveUserHandler)
                .setAllowedOrigins("https://chiu.wiki")
                .addInterceptors(authHandshakeInterceptor)
                .withSockJS();
    }
}