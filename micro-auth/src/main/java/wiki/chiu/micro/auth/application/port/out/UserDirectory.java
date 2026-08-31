package wiki.chiu.micro.auth.application.port.out;

import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

public interface UserDirectory {

    void findByEmail(String email);

    void findByPhone(String phone);

    UserAccessRpcVo findUserAccess(Long userId);

    UserEntityRpcVo findById(Long userId);
}
