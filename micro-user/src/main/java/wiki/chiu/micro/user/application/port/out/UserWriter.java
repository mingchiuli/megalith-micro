package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.User;
import wiki.chiu.micro.user.domain.UserRole;

public interface UserWriter {

    void saveOrUpdate(User user, List<UserRole> roles);

    void deleteUsers(List<Long> ids);
}
