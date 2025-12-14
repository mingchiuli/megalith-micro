package wiki.chiu.micro.user.repository;

import wiki.chiu.micro.user.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    List<UserRoleEntity> findByUserIdIn(List<Long> userIds);

    List<UserRoleEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdIn(List<Long> userIds);

    void deleteByRoleIdIn(List<Long> ids);
}
