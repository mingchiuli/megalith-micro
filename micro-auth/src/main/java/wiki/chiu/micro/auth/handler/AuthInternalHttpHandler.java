package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.common.web.ValidatedRequest;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredHeader;

@Component
public class AuthInternalHttpHandler implements AuthHttpService {

    private final AuthService authService;
    private final ValidatedRequest validation;

    public AuthInternalHttpHandler(AuthService authService, ValidatedRequest validation) {
        this.authService = authService;
        this.validation = validation;
    }

    public ServerResponse getAuthentication(ServerRequest request) {
        return ok(getAuthentication(requiredHeader(request, HttpHeaders.AUTHORIZATION)));
    }

    public ServerResponse getAuthorityRoute(ServerRequest request) throws Exception {
        return ok(getAuthorityRoute(validation.body(request, AuthorityRouteReq.class),
                requiredHeader(request, HttpHeaders.AUTHORIZATION)));
    }

    public ServerResponse routeCheck(ServerRequest request) throws Exception {
        return ok(routeCheck(validation.body(request, AuthorityRouteCheckReq.class),
                requiredHeader(request, HttpHeaders.AUTHORIZATION)));
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
