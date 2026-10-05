package wiki.chiu.micro.auth.application.service;

import static wiki.chiu.micro.common.enums.StatusEnum.NORMAL;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import wiki.chiu.micro.auth.application.model.Menu;
import wiki.chiu.micro.auth.application.model.MenuDisplay;

/**
 * Arranges the flat menu entries of every granted role into the parent/child tree the navigation
 * response expects: enabled entries only, roots ordered by their configured position.
 */
final class MenuTree {

    private static final long ROOT_PARENT_ID = 0L;

    private MenuTree() {
    }

    static List<MenuDisplay> build(List<Menu> menus) {
        List<MenuDisplay> enabled =
            menus.stream()
                .filter(menu -> NORMAL.getCode().equals(menu.status()))
                .distinct()
                .map(MenuDisplay::from)
                .toList();

        return enabled.stream()
            .filter(menu -> menu.parentId() == ROOT_PARENT_ID)
            .map(menu -> menu.withChildren(ROOT_PARENT_ID, childrenOf(menu, enabled)))
            .sorted(Comparator.comparingInt(menu -> position(menu.orderNum())))
            .toList();
    }

    private static List<MenuDisplay> childrenOf(MenuDisplay root, List<MenuDisplay> all) {
        return all.stream()
            .filter(menu -> Objects.equals(menu.parentId(), root.id()))
            .map(menu -> menu.withChildren(root.id(), childrenOf(menu, all)))
            .sorted(Comparator.comparingInt(menu -> position(menu.orderNum())))
            .toList();
    }

    private static int position(Integer orderNum) {
        return Objects.isNull(orderNum) ? 0 : orderNum;
    }
}
