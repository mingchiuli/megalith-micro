package org.chiu.micro.blog.rpc;

import org.chiu.micro.blog.dto.AuthDto;
import org.chiu.micro.blog.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth/{token}")
    Result<AuthDto> getAuthentication(@PathVariable String token);

}
