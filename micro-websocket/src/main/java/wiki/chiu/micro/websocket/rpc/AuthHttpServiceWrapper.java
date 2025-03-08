package wiki.chiu.micro.websocket.rpc;

import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import org.springframework.stereotype.Component;



@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthRpcVo getAuthentication(String token) {
        return Result.handleResult(() -> authHttpService.getAuthentication(token));
    }
}
