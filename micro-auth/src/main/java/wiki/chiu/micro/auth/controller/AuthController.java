package wiki.chiu.micro.auth.controller;

import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.vo.MenusAndButtonsVo;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;


@RestController
@RequestMapping(value = "/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/menu/nav")
    public Result<MenusAndButtonsVo> nav(AuthInfo authInfo) {
        return Result.success(() -> authService.getCurrentUserNav(authInfo.roles()));
    }
}
