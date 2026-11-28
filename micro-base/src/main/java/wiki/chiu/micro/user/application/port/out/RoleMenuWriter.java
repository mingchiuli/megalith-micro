package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.RoleMenu;

public interface RoleMenuWriter {

    void saveMenu(Long roleId, String roleCode, List<RoleMenu> roleMenus);
}
