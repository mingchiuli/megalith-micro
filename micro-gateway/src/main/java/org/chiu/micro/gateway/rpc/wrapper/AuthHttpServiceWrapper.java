package org.chiu.micro.gateway.rpc.wrapper;

import org.chiu.micro.gateway.dto.AuthorityRouteDto;
import org.chiu.micro.gateway.exception.MissException;
import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.req.AuthorityRouteReq;
import org.chiu.micro.gateway.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthorityRouteDto getAuthorityRoute(AuthorityRouteReq req) {
        Result<AuthorityRouteDto> result = authHttpService.getAuthorityRoute(req);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

}
