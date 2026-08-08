package wiki.chiu.micro.user.service;

import java.time.LocalDateTime;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

public interface UserIdentityService {

  void updateLoginTime(String username, LocalDateTime time);

  void changeStatus(String username, Integer status);

  UserEntityRpcVo findById(Long userId);

  UserEntityRpcVo findByEmail(String email);

  UserEntityRpcVo findByPhone(String phone);

  UserEntityRpcVo findByLogin(String login);
}
