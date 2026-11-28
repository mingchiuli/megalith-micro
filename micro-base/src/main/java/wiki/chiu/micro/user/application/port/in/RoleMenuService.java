package wiki.chiu.micro.user.application.port.in;

import java.util.List;

import wiki.chiu.micro.user.application.model.MenuSelection;
import wiki.chiu.micro.user.domain.Menu;

public interface RoleMenuService {

    List<Menu> getCurrentRoleNav(String role);

    List<MenuSelection> getMenusInfo(Long roleId);

    void saveMenu(Long roleId, List<Long> menuIds);
}
