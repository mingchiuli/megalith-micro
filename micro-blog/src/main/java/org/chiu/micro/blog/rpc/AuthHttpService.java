package org.chiu.micro.blog.rpc;

import org.chiu.micro.blog.dto.AuthDto;
import org.chiu.micro.blog.lang.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthDto> getAuthentication(@RequestParam String token);

}
