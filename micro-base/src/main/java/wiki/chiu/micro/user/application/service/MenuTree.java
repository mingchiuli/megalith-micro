package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.enums.StatusEnum.NORMAL;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import wiki.chiu.micro.user.application.model.MenuNode;
import wiki.chiu.micro.user.domain.Menu;

/**
 * Arranges flat menu entries into the parent/child tree the navigation and administration screens
 * expect: root entries first, ordered by their configured position.
 */
final class MenuTree {

    private static final long ROOT_PARENT_ID = 0L;

    private MenuTree() {
    }

    static List<MenuNode> build(List<Menu> menus) {
        return roots(menus, menus);
    }

    static List<MenuNode> buildEnabled(List<Menu> menus) {
        List<Menu> enabled =
            menus.stream().filter(menu -> NORMAL.getCode().equals(menu.status())).toList();
        return roots(enabled, enabled);
    }

    private static List<MenuNode> roots(List<Menu> candidates, List<Menu> all) {
        return candidates.stream()
            .filter(menu -> ROOT_PARENT_ID == menu.parentId())
            .map(menu -> new MenuNode(menu, childrenOf(menu, all)))
            .sorted(Comparator.comparingInt(node -> position(node.menu().orderNum())))
            .toList();
    }

    private static List<MenuNode> childrenOf(Menu root, List<Menu> all) {
        return all.stream()
            .filter(menu -> Objects.equals(menu.parentId(), root.id()))
            .map(menu -> new MenuNode(menu, childrenOf(menu, all)))
            .sorted(Comparator.comparingInt(node -> position(node.menu().orderNum())))
            .toList();
    }

    private static int position(Integer orderNum) {
        return Objects.isNull(orderNum) ? 0 : orderNum;
    }
}
