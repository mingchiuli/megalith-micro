package org.chiu.micro.search.rpc.wrapper;

import org.chiu.micro.search.dto.AuthDto;
import org.chiu.micro.search.exception.MissException;
import org.chiu.micro.search.lang.Result;
import org.chiu.micro.search.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthDto getAuthentication() {
        Result<AuthDto> result = authHttpService.getAuthentication();
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

}
