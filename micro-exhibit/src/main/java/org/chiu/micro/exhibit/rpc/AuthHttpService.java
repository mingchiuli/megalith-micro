package org.chiu.micro.exhibit.rpc;

import org.chiu.micro.exhibit.dto.AuthDto;
import org.chiu.micro.exhibit.lang.Result;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth/{token}")
    Result<AuthDto> getAuthentication(@PathVariable String token);

}
