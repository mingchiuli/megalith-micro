package org.chiu.micro.auth.provider;

import java.util.List;

import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.req.AuthorityRouteReq;
import org.chiu.micro.auth.service.AuthService;
import org.chiu.micro.auth.utils.SecurityAuthenticationUtils;
import org.chiu.micro.auth.vo.AuthorityRouteVo;
import org.chiu.micro.auth.vo.AuthorityVo;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping(value = "/inner/auth")
@RequiredArgsConstructor
@Validated
public class AuthProvider {

    private final SecurityAuthenticationUtils securityAuthenticationUtils;

    private final AuthService authService;

    @GetMapping
    public Result<AuthDto> findById(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token) throws AuthException {
        AuthDto authDto = securityAuthenticationUtils.getAuthDto(token);
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
