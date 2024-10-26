package wiki.chiu.micro.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import wiki.chiu.micro.auth.dto.AuthDto;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.vo.MenusAndButtonsVo;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.lang.Result;
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
        List<String> roles = authDto.roles();
        return Result.success(() -> authService.getCurrentUserNav(roles));
    }
}
