package wiki.chiu.micro.websocket.rpc;

import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.dto.AuthorityRouteRpcDto;
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

    public AuthRpcDto getAuthentication(String token) {
        return Result.handleResult(() -> authHttpService.getAuthentication(token));
    }

    public AuthorityRouteRpcDto getAuthorityRoute(AuthorityRouteReq req, String token) {
        return Result.handleResult(() -> authHttpService.getAuthorityRoute(req, token));
    }


}
