package org.chiu.micro.auth.service.impl;

import org.chiu.micro.auth.convertor.UserInfoVoConvertor;
import org.chiu.micro.auth.dto.UserEntityDto;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.chiu.micro.auth.service.TokenService;
import org.chiu.micro.auth.token.Claims;
import org.chiu.micro.auth.token.TokenUtils;
import org.chiu.micro.auth.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.chiu.micro.auth.lang.Const.TOKEN_PREFIX;

/**
 * @author mingchiuli
 * @create 2023-03-30 4:29 am
 */
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenUtils<Claims> tokenUtils;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    @Value("${blog.jwt.access-token-expire}")
    private long expire;

    public TokenServiceImpl(TokenUtils<Claims> tokenUtils, UserHttpServiceWrapper userHttpServiceWrapper) {
        this.tokenUtils = tokenUtils;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    @Override
    public Map<String, String> refreshToken(Long userId) {
        List<String> roles = userHttpServiceWrapper.findRoleCodesByUserId(userId);
        String accessToken = tokenUtils.generateToken(userId.toString(), roles, expire);
        return Collections.singletonMap("accessToken", TOKEN_PREFIX.getInfo() + accessToken);
    }

    @Override
    public UserInfoVo userinfo(Long userId) {
        UserEntityDto userEntity = userHttpServiceWrapper.findById(userId);
        return UserInfoVoConvertor.convert(userEntity);
    }
}
