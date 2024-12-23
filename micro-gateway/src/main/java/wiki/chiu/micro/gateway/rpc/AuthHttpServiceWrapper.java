package wiki.chiu.micro.gateway.rpc;


import wiki.chiu.micro.common.dto.AuthorityRouteRpcDto;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import org.springframework.stereotype.Component;


@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public Boolean routeCheck(AuthorityRouteCheckReq req, String token) {
        return Result.handleResult(() -> authHttpService.routeCheck(req, token));
    }

    public AuthorityRouteRpcDto getAuthorityRoute(AuthorityRouteReq req) {
        return Result.handleResult(() -> authHttpService.getAuthorityRoute(req));
    }

}
