package org.chiu.micro.websocket.rpc;

import java.util.List;

import org.chiu.micro.websocket.dto.AuthDto;
import org.chiu.micro.websocket.dto.AuthorityDto;
import org.chiu.micro.websocket.lang.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthDto> getAuthentication();

    @PostExchange("/auth/system")
    Result<List<AuthorityDto>> getSystemAuthorities(@RequestBody List<String> service);
}
