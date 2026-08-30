package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import wiki.chiu.micro.user.domain.UserEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;

public interface UserWriter {

  void saveOrUpdate(UserEntity user, List<UserRoleEntity> roles);

  void deleteUsers(List<Long> ids);
}
