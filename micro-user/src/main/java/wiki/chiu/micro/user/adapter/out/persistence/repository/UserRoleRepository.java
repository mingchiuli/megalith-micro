package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.UserRoleEntity;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long>, UserRoleReader {

    List<UserRoleEntity> findByUserIdIn(List<Long> userIds);

    List<UserRoleEntity> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM UserRoleEntity userRole WHERE userRole.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM UserRoleEntity userRole WHERE userRole.userId IN :userIds")
    void deleteByUserIdIn(@Param("userIds") List<Long> userIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM UserRoleEntity userRole WHERE userRole.roleId IN :roleIds")
    void deleteByRoleIdIn(@Param("roleIds") List<Long> roleIds);
}
