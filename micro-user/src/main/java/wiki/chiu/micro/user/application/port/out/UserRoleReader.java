package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.UserRoleEntity;

public interface UserRoleReader {

    List<UserRoleEntity> findAll();

    List<UserRoleEntity> findByUserId(Long userId);

    List<UserRoleEntity> findByUserIdIn(List<Long> userIds);
}
