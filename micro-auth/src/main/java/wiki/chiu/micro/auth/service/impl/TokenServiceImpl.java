package wiki.chiu.micro.auth.service.impl;

import static wiki.chiu.micro.common.lang.Const.TOKEN_PREFIX;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.auth.convertor.UserInfoVoConvertor;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;

/**
 * @author mingchiuli
 * @create 2023-03-30 4:29 am
 */
@Service
public class TokenServiceImpl implements TokenService {

  private final JwtTokenService jwtTokenService;

  private final UserHttpServiceWrapper userHttpServiceWrapper;

  public TokenServiceImpl(
      JwtTokenService jwtTokenService, UserHttpServiceWrapper userHttpServiceWrapper) {
    this.jwtTokenService = jwtTokenService;
    this.userHttpServiceWrapper = userHttpServiceWrapper;
  }

  @Override
  public Map<String, String> refreshToken(Long userId) {
    if (Objects.equals(userId, 0L)) {
      throw new MissException(ExceptionMessage.NO_AUTH);
    }

    if (!StatusEnum.NORMAL.getCode().equals(userHttpServiceWrapper.findById(userId).status())) {
      throw new MissException(ExceptionMessage.NO_AUTH);
    }

    String accessToken = jwtTokenService.issueAccessToken(userId);
    return Collections.singletonMap("accessToken", TOKEN_PREFIX + accessToken);
  }

  @Override
  public UserInfoVo userinfo(Long userId) {
    UserEntityRpcVo userEntity = userHttpServiceWrapper.findById(userId);
    return UserInfoVoConvertor.convert(userEntity);
  }
}
