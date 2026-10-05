package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.UserRole;

public interface UserRoleReader {

    List<UserRole> findAll();

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByUserIdIn(List<Long> userIds);
}
