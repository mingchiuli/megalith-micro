package wiki.chiu.micro.auth.provider;

import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;


@RestController
@RequestMapping(value = "/inner/auth")
@Validated
public class AuthProvider implements AuthHttpService {

    private final AuthService authService;

    public AuthProvider(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public Result<AuthRpcVo> getAuthentication(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token) {
        AuthRpcVo authDto = authService.getAuthVo(token);
        return Result.success(authDto);
    }

    @PostMapping("/route")
    public Result<AuthorityRouteRpcVo> getAuthorityRoute(@RequestBody AuthorityRouteReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token) {
        return Result.success(() -> authService.findRoute(req));
    }

    @PostMapping("/route/check")
    public Result<Boolean> routeCheck(@RequestBody AuthorityRouteCheckReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token) {
        return Result.success(() -> authService.routeCheck(req, token));
    }
}
