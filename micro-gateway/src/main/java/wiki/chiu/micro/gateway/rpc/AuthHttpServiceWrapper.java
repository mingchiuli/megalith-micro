package wiki.chiu.micro.gateway.rpc;

import wiki.chiu.micro.common.dto.AuthorityRouteRpcDto;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;

import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthorityRouteRpcDto getAuthorityRoute(AuthorityRouteReq req) {
        Result<AuthorityRouteRpcDto> result = authHttpService.getAuthorityRoute(req);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
