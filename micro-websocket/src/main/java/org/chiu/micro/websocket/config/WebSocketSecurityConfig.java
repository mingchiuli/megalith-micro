package org.chiu.micro.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

/**
 * 支持security注解@PreAuthorize
 * @author mingchiuli
 * @create 2023-01-15 11:38 am
 */
@Configuration
public class WebSocketSecurityConfig {

    @Bean
    AuthorizationManager<Message<?>> authorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        return messages
                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.OTHER)
                .permitAll()
                .simpDestMatchers("/edit/ws/push/action")
                .hasAuthority("sys:edit:push:action")
                .anyMessage()
                .authenticated()
                .build();
    }
}
