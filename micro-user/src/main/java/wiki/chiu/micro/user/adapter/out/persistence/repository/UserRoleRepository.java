package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.UserRoleEntity;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long>, UserRoleReader {

  List<UserRoleEntity> findByUserIdIn(List<Long> userIds);

  List<UserRoleEntity> findByUserId(Long userId);

  void deleteByUserId(Long userId);

  void deleteByUserIdIn(List<Long> userIds);

  void deleteByRoleIdIn(List<Long> ids);
}
