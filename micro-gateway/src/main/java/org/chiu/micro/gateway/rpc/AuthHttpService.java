package org.chiu.micro.gateway.rpc;

import org.chiu.micro.gateway.dto.AuthDto;
import org.chiu.micro.gateway.lang.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthDto> getAuthentication(@RequestParam String token);

}
