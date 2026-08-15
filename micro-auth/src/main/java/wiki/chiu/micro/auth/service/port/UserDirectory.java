package wiki.chiu.micro.auth.service.port;

import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

public interface UserDirectory {

  void findByEmail(String email);

  void findByPhone(String phone);

  UserAccessRpcVo findUserAccess(Long userId);

  UserEntityRpcVo findById(Long userId);
}
