package org.chiu.micro.auth.controller;

import java.util.List;

import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.service.AuthMenuService;
import org.chiu.micro.auth.utils.SecurityAuthenticationUtils;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
public class AuthController {

    private final AuthMenuService authMenuService;

    private final SecurityAuthenticationUtils securityAuthenticationUtils;

    @GetMapping("/menu/nav")
    public Result<MenusAndButtonsVo> nav(HttpServletRequest request) {
        AuthDto authDto = securityAuthenticationUtils.getAuthDto(request.getHeader(HttpHeaders.AUTHORIZATION));
        List<String> roles = authDto.getRoles();
        return Result.success(() -> authMenuService.getCurrentUserNav(roles));
    }
}
