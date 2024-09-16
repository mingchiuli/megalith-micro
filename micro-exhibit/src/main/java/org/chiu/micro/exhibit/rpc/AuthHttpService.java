package org.chiu.micro.exhibit.rpc;

import org.chiu.micro.exhibit.dto.AuthDto;
import org.chiu.micro.exhibit.lang.Result;

import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthDto> getAuthentication();

}
