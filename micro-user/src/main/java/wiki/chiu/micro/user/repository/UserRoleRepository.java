package wiki.chiu.micro.user.repository;

import wiki.chiu.micro.user.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    List<UserRoleEntity> findByUserIdIn(List<Long> userIds);

    List<UserRoleEntity> findByUserId(Long userId);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

    @Modifying
    @Transactional
    void deleteByUserIdIn(List<Long> userIds);

    @Modifying
    @Transactional
    void deleteByRoleIdIn(List<Long> ids);
}
