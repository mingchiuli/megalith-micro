package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.RoleMenu;

public interface RoleMenuReader {

    List<RoleMenu> findAll();

    List<RoleMenu> findByRoleIdIn(List<Long> roleIds);

    List<Long> findMenuIdsByRoleId(Long roleId);
}
