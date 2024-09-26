package org.chiu.micro.websocket.rpc.wrapper;

import org.chiu.micro.websocket.dto.AuthDto;
import org.chiu.micro.websocket.dto.AuthorityRouteDto;
import org.chiu.micro.websocket.exception.MissException;
import org.chiu.micro.websocket.lang.Result;
import org.chiu.micro.websocket.req.AuthorityRouteReq;
import org.chiu.micro.websocket.rpc.AuthHttpService;
import org.springframework.stereotype.Component;


@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthDto getAuthentication(String token) {
        Result<AuthDto> result = authHttpService.getAuthentication(token);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public AuthorityRouteDto getAuthorityRoute(AuthorityRouteReq req, String token) {
        Result<AuthorityRouteDto> result = authHttpService.getAuthorityRoute(req, token);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
