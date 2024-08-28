package org.chiu.micro.websocket.config.interceptor;


import lombok.RequiredArgsConstructor;

import org.chiu.micro.websocket.dto.AuthDto;
import org.chiu.micro.websocket.rpc.wrapper.AuthHttpServiceWrapper;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author mingchiuli
 * @create 2022-06-17 9:46 PM
 */
@Component
@RequiredArgsConstructor
public class MessageInterceptor implements ChannelInterceptor {

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    @SuppressWarnings("null")
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (!StringUtils.hasLength(token)) {
                return message;
            }
            
            AuthDto authDto = authHttpServiceWrapper.getAuthentication(token);
            PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(authDto.getUserId(), null, AuthorityUtils.createAuthorityList(authDto.getAuthorities()));
            accessor.setUser(authentication);
        }
        return message;
    }

}