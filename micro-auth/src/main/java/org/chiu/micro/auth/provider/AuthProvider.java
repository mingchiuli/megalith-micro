package org.chiu.micro.auth.provider;

import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.req.AuthorityRouteReq;
import org.chiu.micro.auth.service.AuthService;
import org.chiu.micro.auth.vo.AuthorityRouteVo;
import org.chiu.micro.auth.vo.AuthorityVo;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/inner/auth")
@Validated
public class AuthProvider {

    private final AuthService authService;

    public AuthProvider(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public Result<AuthDto> findAuth(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token) {
        AuthDto authDto = authService.getAuthDto(token);
        return Result.success(authDto);
    }

    @PostMapping("/system")
    public Result<List<AuthorityVo>> system(@RequestBody List<String> service) {
        return Result.success(() -> authService.getSystemAuthority(service));
    }

    @PostMapping("/route")
    public Result<AuthorityRouteVo> route(@RequestBody AuthorityRouteReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token) {
        return Result.success(() -> authService.route(req, token));
    }
}
