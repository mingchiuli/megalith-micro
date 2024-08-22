package org.chiu.micro.websocket.rpc.wrapper;

import org.chiu.micro.websocket.dto.AuthDto;
import org.chiu.micro.websocket.exception.MissException;
import org.chiu.micro.websocket.lang.Result;
import org.chiu.micro.websocket.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthDto getAuthentication(String token) {
        Result<AuthDto> result = authHttpService.getAuthentication(token);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

}
