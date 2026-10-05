package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.RoleDataPermission;

public interface RoleDataPermissionReader {

    List<RoleDataPermission> findAll();

    List<RoleDataPermission> findByRoleId(Long roleId);

    List<RoleDataPermission> findByRoleIdIn(List<Long> roleIds);
}
