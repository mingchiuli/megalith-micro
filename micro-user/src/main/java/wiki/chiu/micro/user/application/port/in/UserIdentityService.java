package wiki.chiu.micro.user.application.port.in;

import java.time.LocalDateTime;

import wiki.chiu.micro.user.application.model.UserAccess;
import wiki.chiu.micro.user.domain.User;

public interface UserIdentityService {

    void updateLoginTime(String username, LocalDateTime time);

    void lockAfterPasswordFailures(Long userId);

    int unlockExpiredBatch();

    User findById(Long userId);

    User findByEmail(String email);

    User findByPhone(String phone);

    User findByLogin(String login);

    UserAccess findUserAccess(Long userId);
}
