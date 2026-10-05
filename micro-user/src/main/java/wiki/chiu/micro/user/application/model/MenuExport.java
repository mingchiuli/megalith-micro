package wiki.chiu.micro.user.application.model;

import java.util.List;

import wiki.chiu.micro.user.domain.Menu;
import wiki.chiu.micro.user.domain.RoleMenu;

/**
 * The rows the menu export delivers: the menus with their role assignments.
 */
public record MenuExport(List<Menu> menus, List<RoleMenu> roleMenus) {
}
