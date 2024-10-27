package wiki.chiu.micro.auth.service.impl;

import wiki.chiu.micro.auth.convertor.UserInfoVoConvertor;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.auth.token.Claims;
import wiki.chiu.micro.auth.token.TokenUtils;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.dto.UserEntityRpcDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static wiki.chiu.micro.common.lang.Const.TOKEN_PREFIX;

/**
 * @author mingchiuli
 * @create 2023-03-30 4:29 am
 */
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenUtils<Claims> tokenUtils;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    @Value("${megalith.blog.jwt.access-token-expire}")
    private long expire;

    public TokenServiceImpl(TokenUtils<Claims> tokenUtils, UserHttpServiceWrapper userHttpServiceWrapper) {
        this.tokenUtils = tokenUtils;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    @Override
    public Map<String, String> refreshToken(Long userId) {
        List<String> roles = userHttpServiceWrapper.findRoleCodesByUserId(userId);
        String accessToken = tokenUtils.generateToken(userId.toString(), roles, expire);
        return Collections.singletonMap("accessToken", TOKEN_PREFIX + accessToken);
    }

    @Override
    public UserInfoVo userinfo(Long userId) {
        UserEntityRpcDto userEntity = userHttpServiceWrapper.findById(userId);
        return UserInfoVoConvertor.convert(userEntity);
    }
}