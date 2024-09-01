package org.chiu.micro.gateway.rpc;

import java.util.List;

import org.chiu.micro.gateway.dto.AuthorityDto;
import org.chiu.micro.gateway.lang.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface UserHttpService {

    @GetExchange("/authority/list")
    Result<List<AuthorityDto>> getAuthorities(@RequestParam List<String> service);
}
