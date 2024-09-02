package org.chiu.micro.gateway.rpc;

import java.util.List;

import org.chiu.micro.gateway.dto.AuthDto;
import org.chiu.micro.gateway.dto.AuthorityDto;
import org.chiu.micro.gateway.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth/{token}")
    Result<AuthDto> getAuthentication(@PathVariable String token);

    @GetExchange("/auth/system")
    Result<List<AuthorityDto>> getSystemAuthorities(@RequestBody List<String> service);
}
