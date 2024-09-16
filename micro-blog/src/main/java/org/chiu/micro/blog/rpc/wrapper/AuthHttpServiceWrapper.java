package org.chiu.micro.blog.rpc.wrapper;

import org.chiu.micro.blog.dto.AuthDto;
import org.chiu.micro.blog.exception.AuthException;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    @SneakyThrows
    public AuthDto getAuthentication() {
        Result<AuthDto> result = authHttpService.getAuthentication();
        if (result.getCode() != 200) {
            throw new AuthException(result.getMsg());
        }
        return result.getData();
    }

}
