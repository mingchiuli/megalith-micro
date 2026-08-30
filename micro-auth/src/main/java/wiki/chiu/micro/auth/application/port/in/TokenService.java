package wiki.chiu.micro.auth.application.port.in;

import wiki.chiu.micro.auth.vo.UserInfoVo;

/**
 * @author mingchiuli
 * @create 2023-03-30 4:29 am
 */
public interface TokenService {

  String refreshAccessToken(Long userId);

  UserInfoVo userinfo(Long userId);
}
