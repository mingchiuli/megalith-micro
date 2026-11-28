package wiki.chiu.micro.user.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface UserIdentityWriter {

    void updateLoginTime(String login, LocalDateTime time);

    void lockAfterPasswordFailures(Long userId);

    int unlockExpired(List<Long> userIds);
}
