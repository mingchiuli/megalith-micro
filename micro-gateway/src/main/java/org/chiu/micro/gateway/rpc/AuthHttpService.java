package org.chiu.micro.gateway.rpc;

import java.util.List;

import org.chiu.micro.gateway.dto.AuthDto;
import org.chiu.micro.gateway.dto.AuthorityDto;
import org.chiu.micro.gateway.lang.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthDto> getAuthentication(@RequestParam String token);

    @GetExchange("/auth/system")
    Result<List<AuthorityDto>> getSystemAuthorities(@RequestParam List<String> service);
}
