package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import wiki.chiu.micro.user.domain.RoleMenuEntity;

public interface RoleMenuReader {

  List<RoleMenuEntity> findAll();

  List<RoleMenuEntity> findByRoleIdIn(List<Long> roleIds);

  List<Long> findMenuIdsByRoleId(Long roleId);
}
