package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.rpc.AuthHttpService;

import static wiki.chiu.micro.common.web.FunctionalWeb.authInfo;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

@Component
public class TokenHttpHandler {

    private final TokenService tokenService;
    private final AuthHttpService authHttpService;

    public TokenHttpHandler(TokenService tokenService, AuthHttpService authHttpService) {
        this.tokenService = tokenService;
        this.authHttpService = authHttpService;
    }

    public ServerResponse refreshToken(ServerRequest request) {
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> tokenService.refreshToken(authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse userinfo(ServerRequest request) {
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> tokenService.userinfo(authInfo.userId())));
    }
}
