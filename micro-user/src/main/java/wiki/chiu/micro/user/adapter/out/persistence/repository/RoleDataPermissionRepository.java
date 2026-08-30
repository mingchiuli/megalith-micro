package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;

public interface RoleDataPermissionRepository
    extends JpaRepository<RoleDataPermissionEntity, Long>, RoleDataPermissionReader {

  List<RoleDataPermissionEntity> findByRoleId(Long roleId);

  List<RoleDataPermissionEntity> findByRoleIdIn(List<Long> roleIds);

  void deleteByRoleId(Long roleId);

  void deleteByRoleIdIn(List<Long> roleIds);
}
