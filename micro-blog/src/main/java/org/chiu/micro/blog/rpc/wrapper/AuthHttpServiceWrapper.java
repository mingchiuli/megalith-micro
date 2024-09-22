package org.chiu.micro.blog.rpc.wrapper;

import org.chiu.micro.blog.dto.AuthDto;
import org.chiu.micro.blog.exception.AuthException;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthDto getAuthentication() throws AuthException {
        Result<AuthDto> result = authHttpService.getAuthentication();
        if (result.getCode() != 200) {
            throw new AuthException(result.getMsg());
        }
        return result.getData();
    }

}
