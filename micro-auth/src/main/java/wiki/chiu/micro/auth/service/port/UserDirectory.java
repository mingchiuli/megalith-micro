package wiki.chiu.micro.auth.service.port;

import java.util.List;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

public interface UserDirectory {

  void findByEmail(String email);

  void findByPhone(String phone);

  List<String> findRoleCodesByUserId(Long userId);

  UserEntityRpcVo findById(Long userId);
}
