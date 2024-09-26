package org.chiu.micro.websocket.rpc;


import org.chiu.micro.websocket.dto.AuthDto;
import org.chiu.micro.websocket.dto.AuthorityRouteDto;
import org.chiu.micro.websocket.lang.Result;
import org.chiu.micro.websocket.req.AuthorityRouteReq;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthDto> getAuthentication(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);


    @PostExchange("/auth/route")
    Result<AuthorityRouteDto> getAuthorityRoute(@RequestBody AuthorityRouteReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);
}
