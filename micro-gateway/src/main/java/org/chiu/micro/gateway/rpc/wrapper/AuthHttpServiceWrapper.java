package org.chiu.micro.gateway.rpc.wrapper;

import org.chiu.micro.gateway.dto.AuthorityRouteDto;
import org.chiu.micro.gateway.exception.MissException;
import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.req.AuthorityRouteReq;
import org.chiu.micro.gateway.rpc.AuthHttpService;

import org.springframework.stereotype.Component;

@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthorityRouteDto getAuthorityRoute(AuthorityRouteReq req) {
        Result<AuthorityRouteDto> result = authHttpService.getAuthorityRoute(req);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
