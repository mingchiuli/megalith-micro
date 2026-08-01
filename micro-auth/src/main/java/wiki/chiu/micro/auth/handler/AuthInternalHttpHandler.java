package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;

@Component
public class AuthInternalHttpHandler implements AuthHttpService {

    private final AuthService authService;

    public AuthInternalHttpHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Result<AuthRpcVo> getAuthentication(String token) {
        return Result.success(authService.getAuthVo(token));
    }

    @Override
    public Result<AuthorityRouteRpcVo> getAuthorityRoute(AuthorityRouteReq req, String token) {
        return Result.success(() -> authService.findRoute(req));
    }

    @Override
    public Result<Boolean> routeCheck(AuthorityRouteCheckReq req, String token) {
        return Result.success(() -> authService.routeCheck(req, token));
    }
}
