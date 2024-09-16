package org.chiu.micro.exhibit.rpc.wrapper;

import org.chiu.micro.exhibit.dto.AuthDto;
import org.chiu.micro.exhibit.exception.MissException;
import org.chiu.micro.exhibit.lang.Result;
import org.chiu.micro.exhibit.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthDto getAuthentication() {
        Result<AuthDto> result = authHttpService.getAuthentication();
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

}
