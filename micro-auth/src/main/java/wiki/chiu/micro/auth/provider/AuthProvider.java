package wiki.chiu.micro.auth.provider;

import wiki.chiu.micro.auth.dto.AuthDto;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.vo.AuthorityRouteVo;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.req.AuthorityRouteReq;


@RestController
@RequestMapping(value = "/inner/auth")
@Validated
public class AuthProvider {

    private final AuthService authService;

    public AuthProvider(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public Result<AuthDto> findAuth(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token) throws AuthException {
        AuthDto authDto = authService.getAuthDto(token);
        return Result.success(authDto);
    }

    @PostMapping("/route")
    public Result<AuthorityRouteVo> route(@RequestBody AuthorityRouteReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token) {
        return Result.success(() -> authService.route(req, token));
    }
}
