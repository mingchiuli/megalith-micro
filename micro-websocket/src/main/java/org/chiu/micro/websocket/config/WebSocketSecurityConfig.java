package org.chiu.micro.websocket.config;

import org.chiu.micro.websocket.dto.AuthorityDto;
import org.chiu.micro.websocket.lang.Const;
import org.chiu.micro.websocket.rpc.wrapper.AuthHttpServiceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

import java.util.List;

/**
 * 支持security注解@PreAuthorize
 *
 * @author mingchiuli
 * @create 2023-01-15 11:38 am
 */
@Configuration
public class WebSocketSecurityConfig {

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public WebSocketSecurityConfig(AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }

    @Bean
    AuthorizationManager<Message<?>> authorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {

        List<AuthorityDto> authorities = authHttpServiceWrapper.getSystemAuthorities().stream()
                .filter(item -> Const.WS.getInfo().equals(item.prototype()))
                .filter(item -> !item.code().startsWith(Const.WHITELIST.getInfo()))
                .toList();

        var builder = messages
                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.OTHER)
                .permitAll();

        authorities.forEach(authority -> builder
                .simpDestMatchers(authority.routePattern())
                .hasAuthority(authority.code()));

        return builder
                .anyMessage()
                .authenticated()
                .build();
    }
}
