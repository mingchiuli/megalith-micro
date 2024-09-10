package org.chiu.micro.gateway.rpc;

import org.chiu.micro.gateway.dto.AuthorityRouteDto;
import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.req.AuthorityRouteReq;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.PostExchange;


public interface AuthHttpService {

    @PostExchange("/auth/route")
    Result<AuthorityRouteDto> getAuthorityRoute(@RequestBody AuthorityRouteReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = "") String token);
}
