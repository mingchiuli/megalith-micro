package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;

public interface RoleDataPermissionWriter {

  void saveDataPermissions(Long roleId, List<RoleDataPermissionEntity> permissions);
}
