package wiki.chiu.micro.auth.service;

import java.util.List;
import java.util.Map;

import wiki.chiu.micro.auth.vo.UserInfoVo;


/**
 * @author mingchiuli
 * @create 2023-03-30 4:29 am
 */
public interface TokenService {

    Map<String, String> refreshToken(Long userId, List<String> tokenRoles);

    UserInfoVo userinfo(Long userId);
}
