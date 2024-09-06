package org.chiu.micro.search.rpc;

import org.chiu.micro.search.dto.AuthDto;
import org.chiu.micro.search.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth/{token}")
    Result<AuthDto> getAuthentication(@PathVariable String token);

}
