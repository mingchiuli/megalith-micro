package org.chiu.micro.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.service.AuthService;
import org.chiu.micro.auth.service.TokenService;
import org.chiu.micro.auth.vo.UserInfoVo;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author mingchiuli
 * @create 2023-03-29 12:58 am
 */
@RestController
@RequestMapping("/token")
public class TokenController {

    private final TokenService tokenService;

    private final AuthService authService;

    public TokenController(TokenService tokenService, AuthService authService) {
        this.tokenService = tokenService;
        this.authService = authService;
    }


    @GetMapping("/refresh")
    public Result<Map<String, String>> refreshToken(HttpServletRequest request) throws AuthException {
        AuthDto authDto = authService.getAuthDto(request.getHeader(HttpHeaders.AUTHORIZATION));
        Map<String, String> stringStringMap = tokenService.refreshToken(authDto.getUserId());
        return Result.success(stringStringMap);
    }

    @GetMapping("/userinfo")
    public Result<UserInfoVo> userinfo(HttpServletRequest request) throws AuthException {
        AuthDto authDto = authService.getAuthDto(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> tokenService.userinfo(authDto.getUserId()));
    }
}
