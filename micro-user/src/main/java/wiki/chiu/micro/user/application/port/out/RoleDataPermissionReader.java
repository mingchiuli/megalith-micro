package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;

public interface RoleDataPermissionReader {

    List<RoleDataPermissionEntity> findAll();

    List<RoleDataPermissionEntity> findByRoleId(Long roleId);

    List<RoleDataPermissionEntity> findByRoleIdIn(List<Long> roleIds);
}
