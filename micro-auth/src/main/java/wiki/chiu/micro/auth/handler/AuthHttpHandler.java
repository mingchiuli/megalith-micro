package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.rpc.AuthHttpService;

import static wiki.chiu.micro.common.web.FunctionalWeb.authInfo;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

@Component
public class AuthHttpHandler {

    private final AuthService authService;
    private final AuthHttpService authHttpService;

    public AuthHttpHandler(AuthService authService, AuthHttpService authHttpService) {
        this.authService = authService;
        this.authHttpService = authHttpService;
    }

    public ServerResponse nav(ServerRequest request) {
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> authService.getCurrentUserNav(authInfo.roles())));
    }
}
