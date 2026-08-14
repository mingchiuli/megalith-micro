package wiki.chiu.micro.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import wiki.chiu.micro.user.entity.RoleDataPermissionEntity;

public interface RoleDataPermissionRepository
    extends JpaRepository<RoleDataPermissionEntity, Long> {

  List<RoleDataPermissionEntity> findByRoleId(Long roleId);

  List<RoleDataPermissionEntity> findByRoleIdIn(List<Long> roleIds);

  void deleteByRoleId(Long roleId);

  void deleteByRoleIdIn(List<Long> roleIds);
}
