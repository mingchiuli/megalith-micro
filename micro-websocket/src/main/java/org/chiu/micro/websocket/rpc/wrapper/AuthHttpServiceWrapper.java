package org.chiu.micro.websocket.rpc.wrapper;

import org.chiu.micro.websocket.dto.AuthDto;
import org.chiu.micro.websocket.dto.AuthorityDto;
import org.chiu.micro.websocket.exception.MissException;
import org.chiu.micro.websocket.lang.Const;
import org.chiu.micro.websocket.lang.Result;
import org.chiu.micro.websocket.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthDto getAuthentication(String token) {
        Result<AuthDto> result = authHttpService.getAuthentication(token);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public List<AuthorityDto> getSystemAuthorities() {
        Result<List<AuthorityDto>> result = authHttpService.getSystemAuthorities(Collections.singletonList(Const.WEBSOCKET_SERVICE.getInfo()));
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
