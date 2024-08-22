package org.chiu.micro.gateway.rpc.wrapper;

import org.chiu.micro.gateway.dto.AuthDto;
import org.chiu.micro.gateway.exception.AuthException;
import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthDto getAuthentication(String token) throws AuthException {
        Result<AuthDto> result = authHttpService.getAuthentication(token);
        
        if (result.getCode() != 200) {
            throw new AuthException(result.getMsg());
        }
        return result.getData();
    }

}
