package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.RoleDataPermission;

public interface RoleDataPermissionWriter {

    void saveDataPermissions(Long roleId, List<RoleDataPermission> permissions);
}
