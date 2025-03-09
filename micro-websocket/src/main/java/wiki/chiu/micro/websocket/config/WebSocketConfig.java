package wiki.chiu.micro.websocket.config;

import wiki.chiu.micro.websocket.config.handler.EditWebSocketHandler;
import wiki.chiu.micro.websocket.config.handler.RetrieveUserHandler;

import org.springframework.beans.factory.annotation.Value;
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

    private final RetrieveUserHandler retrieveUserHandler;

    public WebSocketConfig(EditWebSocketHandler editWebSocketHandler, RetrieveUserHandler retrieveUserHandler) {
        this.editWebSocketHandler = editWebSocketHandler;
        this.retrieveUserHandler = retrieveUserHandler;
    }
    
    @Value("${megalith.gateway.base-url}")
    private String gatewayUrl;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(editWebSocketHandler, "/edit/ws")
                .setHandshakeHandler(retrieveUserHandler)
                .setAllowedOrigins(gatewayUrl);
    }
}