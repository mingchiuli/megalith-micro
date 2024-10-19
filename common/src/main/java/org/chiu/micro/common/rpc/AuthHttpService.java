package org.chiu.micro.common.rpc;


import org.chiu.micro.common.dto.AuthRpcDto;
import org.chiu.micro.common.dto.AuthorityRouteRpcDto;
import org.chiu.micro.common.lang.Result;
import org.chiu.micro.common.req.AuthorityRouteReq;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthRpcDto> getAuthentication();

    @GetExchange("/auth")
    Result<AuthRpcDto> getAuthentication(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/auth/route")
    Result<AuthorityRouteRpcDto> getAuthorityRoute(@RequestBody AuthorityRouteReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/auth/route")
    Result<AuthorityRouteRpcDto> getAuthorityRoute(@RequestBody AuthorityRouteReq req);
}