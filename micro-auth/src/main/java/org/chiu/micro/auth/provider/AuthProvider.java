package org.chiu.micro.auth.provider;

import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.req.AuthorityRouteReq;
import org.chiu.micro.auth.service.AuthService;
import org.chiu.micro.auth.vo.AuthorityRouteVo;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



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
