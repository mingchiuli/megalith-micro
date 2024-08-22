package org.chiu.micro.websocket.config.interceptor;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component(value = "csrfChannelInterceptor")
public class CSRFChannelInterceptor implements ChannelInterceptor {

    //disabled csrf
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        return ChannelInterceptor.super.preSend(message, channel);
    }
}
