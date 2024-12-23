package wiki.chiu.micro.websocket.rpc;

import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
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

    public Boolean routeCheck(AuthorityRouteCheckReq req, String token) {
        return Result.handleResult(() -> authHttpService.routeCheck(req, token));
    }


}
