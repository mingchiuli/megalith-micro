package org.chiu.micro.websocket.config;

import java.util.List;

import org.chiu.micro.websocket.dto.AuthorityDto;
import org.chiu.micro.websocket.lang.Const;
import org.chiu.micro.websocket.rpc.wrapper.UserHttpServiceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

import lombok.RequiredArgsConstructor;

/**
 * 支持security注解@PreAuthorize
 *
 * @author mingchiuli
 * @create 2023-01-15 11:38 am
 */
@Configuration
@RequiredArgsConstructor
public class WebSocketSecurityConfig {

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    @Bean
    AuthorizationManager<Message<?>> authorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {

        List<AuthorityDto> authorities = userHttpServiceWrapper.getAuthorities().stream()
                .filter(item -> Const.WS.getInfo().equals(item.getPrototype()))
                .filter(item -> !item.getCode().startsWith(Const.WHITELIST.getInfo()))
                .toList();

        var builder = messages
                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.OTHER)
                .permitAll();

        authorities.forEach(authority -> builder
                .simpDestMatchers(authority.getRoutePattern())
                .hasAuthority(authority.getCode()));

        return builder
                .anyMessage()
                .authenticated()
                .build();
    }
}
