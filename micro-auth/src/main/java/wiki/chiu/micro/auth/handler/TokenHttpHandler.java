package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;

import java.util.Map;

@Component
public class TokenHttpHandler {

    private final TokenService tokenService;

    public TokenHttpHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public Result<Map<String, String>> refreshToken(AuthInfo authInfo) {
        return Result.success(() -> tokenService.refreshToken(authInfo.userId(), authInfo.roles()));
    }

    public Result<UserInfoVo> userinfo(AuthInfo authInfo) {
        return Result.success(() -> tokenService.userinfo(authInfo.userId()));
    }
}
