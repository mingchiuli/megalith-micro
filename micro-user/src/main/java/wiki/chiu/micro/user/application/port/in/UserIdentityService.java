package wiki.chiu.micro.user.application.port.in;

import java.time.LocalDateTime;

import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

public interface UserIdentityService {

    void updateLoginTime(String username, LocalDateTime time);

    void lockAfterPasswordFailures(Long userId);

    int unlockExpiredBatch();

    UserEntityRpcVo findById(Long userId);

    UserAccessRpcVo findUserAccess(Long userId);

    UserEntityRpcVo findByEmail(String email);

    UserEntityRpcVo findByPhone(String phone);

    UserEntityRpcVo findByLogin(String login);
}
