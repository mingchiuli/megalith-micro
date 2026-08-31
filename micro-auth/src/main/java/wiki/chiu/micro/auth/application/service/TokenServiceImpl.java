package wiki.chiu.micro.auth.application.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.auth.application.port.in.TokenService;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;
import wiki.chiu.micro.auth.convertor.UserInfoVoConvertor;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

/**
 * @author mingchiuli
 * @create 2023-03-30 4:29 am
 */
@Service
public class TokenServiceImpl implements TokenService {

    private final JwtTokenService jwtTokenService;

    private final UserDirectory users;

    public TokenServiceImpl(JwtTokenService jwtTokenService, UserDirectory users) {
        this.jwtTokenService = jwtTokenService;
        this.users = users;
    }

    @Override
    public String refreshAccessToken(Long userId) {
        if (Objects.equals(userId, 0L)) {
            throw new MissException(ExceptionMessage.NO_AUTH);
        }

        if (!StatusEnum.NORMAL.getCode().equals(users.findById(userId).status())) {
            throw new MissException(ExceptionMessage.NO_AUTH);
        }

        return jwtTokenService.issueAccessToken(userId);
    }

    @Override
    public UserInfoVo userinfo(Long userId) {
        UserEntityRpcVo userEntity = users.findById(userId);
        return UserInfoVoConvertor.convert(userEntity);
    }
}
