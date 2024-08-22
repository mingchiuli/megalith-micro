package org.chiu.micro.search.rpc;

import org.chiu.micro.search.dto.AuthDto;
import org.chiu.micro.search.lang.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthDto> getAuthentication(@RequestParam String token);

}
