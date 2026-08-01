package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;

@Component
public class AuthHttpHandler {

    private final AuthService authService;

    public AuthHttpHandler(AuthService authService) {
        this.authService = authService;
    }

    public Result<MenuWithChildVo> nav(AuthInfo authInfo) {
        return Result.success(() -> authService.getCurrentUserNav(authInfo.roles()));
    }
}
