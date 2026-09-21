package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;

public interface RoleDataPermissionRepository
    extends JpaRepository<RoleDataPermissionEntity, Long>, RoleDataPermissionReader {

    List<RoleDataPermissionEntity> findByRoleId(Long roleId);

    List<RoleDataPermissionEntity> findByRoleIdIn(List<Long> roleIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        "DELETE FROM RoleDataPermissionEntity roleDataPermission WHERE roleDataPermission.roleId ="
            + " :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        "DELETE FROM RoleDataPermissionEntity roleDataPermission WHERE roleDataPermission.roleId IN"
            + " :roleIds")
    void deleteByRoleIdIn(@Param("roleIds") List<Long> roleIds);
}
