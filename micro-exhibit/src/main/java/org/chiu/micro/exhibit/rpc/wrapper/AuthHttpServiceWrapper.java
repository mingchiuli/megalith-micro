package org.chiu.micro.exhibit.rpc.wrapper;

import org.chiu.micro.exhibit.dto.AuthDto;
import org.chiu.micro.exhibit.exception.MissException;
import org.chiu.micro.exhibit.lang.Result;
import org.chiu.micro.exhibit.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthDto getAuthentication() {
        Result<AuthDto> result = authHttpService.getAuthentication();
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
