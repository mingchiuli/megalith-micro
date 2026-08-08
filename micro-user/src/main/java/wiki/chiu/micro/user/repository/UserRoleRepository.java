package wiki.chiu.micro.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.UserRoleEntity;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

  List<UserRoleEntity> findByUserIdIn(List<Long> userIds);

  List<UserRoleEntity> findByUserId(Long userId);

  @Transactional
  void deleteByUserId(Long userId);

  @Transactional
  void deleteByUserIdIn(List<Long> userIds);

  @Transactional
  void deleteByRoleIdIn(List<Long> ids);
}
