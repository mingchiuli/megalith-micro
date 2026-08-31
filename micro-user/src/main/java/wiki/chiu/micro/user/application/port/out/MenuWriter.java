package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.MenuEntity;

public interface MenuWriter {

    void saveMenus(List<MenuEntity> menus, List<Long> roleIds, List<String> roleCodes);

    void deleteMenu(Long id, List<Long> roleIds, List<String> roleCodes);
}
