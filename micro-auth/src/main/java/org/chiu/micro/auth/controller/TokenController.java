package org.chiu.micro.auth.controller;

import lombok.RequiredArgsConstructor;

import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.service.TokenService;
import org.chiu.micro.auth.utils.SecurityAuthenticationUtils;
import org.chiu.micro.auth.vo.UserInfoVo;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @author mingchiuli
 * @create 2023-03-29 12:58 am
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/token")
public class TokenController {

    private final TokenService tokenService;

    private final SecurityAuthenticationUtils securityAuthenticationUtils;


    @GetMapping("/refresh")
    public Result<Map<String, String>> refreshToken(HttpServletRequest request) throws AuthException {
        AuthDto authDto = securityAuthenticationUtils.getAuthDto(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> tokenService.refreshToken(authDto.getUserId()));
    }

    @GetMapping("/userinfo")
    public Result<UserInfoVo> userinfo(HttpServletRequest request) throws AuthException {
        AuthDto authDto = securityAuthenticationUtils.getAuthDto(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> tokenService.userinfo(authDto.getUserId()));
    }
}
