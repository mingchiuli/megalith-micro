package org.chiu.micro.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.service.AuthService;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/menu/nav")
    public Result<MenusAndButtonsVo> nav(HttpServletRequest request) throws AuthException {
        AuthDto authDto = authService.getAuthDto(request.getHeader(HttpHeaders.AUTHORIZATION));
        List<String> roles = authDto.getRoles();
        return Result.success(() -> authService.getCurrentUserNav(roles));
    }
}
