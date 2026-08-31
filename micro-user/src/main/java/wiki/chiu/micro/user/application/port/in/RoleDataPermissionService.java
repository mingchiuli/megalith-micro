package wiki.chiu.micro.user.application.port.in;

import java.util.List;

import wiki.chiu.micro.common.lang.DataPermissionEnum;

public interface RoleDataPermissionService {

    List<DataPermissionEnum> getDataPermissions(Long roleId);

    void saveDataPermissions(Long roleId, List<DataPermissionEnum> dataPermissions);
}
