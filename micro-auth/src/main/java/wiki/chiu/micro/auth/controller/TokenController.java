package wiki.chiu.micro.auth.controller;

import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wiki.chiu.micro.common.resolver.AuthInfo;

import java.util.Map;

/**
 * @author mingchiuli
 * @create 2023-03-29 12:58 am
 */
@RestController
@RequestMapping("/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    @GetMapping("/refresh")
    public Result<Map<String, String>> refreshToken(AuthInfo authInfo) {
        return Result.success(() -> tokenService.refreshToken(authInfo.userId()));
    }

    @GetMapping("/userinfo")
    public Result<UserInfoVo> userinfo(AuthInfo authInfo) {
        return Result.success(() -> tokenService.userinfo(authInfo.userId()));
    }
}
