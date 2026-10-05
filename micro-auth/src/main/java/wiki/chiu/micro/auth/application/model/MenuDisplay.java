package wiki.chiu.micro.auth.application.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A menu entry arranged in the parent/child tree the navigation response needs.
 */
public record MenuDisplay(
    Long id,
    Long parentId,
    String title,
    String name,
    String url,
    String component,
    Integer type,
    String icon,
    Integer orderNum,
    Integer status,
    List<MenuDisplay> children) {

    public static MenuDisplay from(Menu menu) {
        return new MenuDisplay(
            menu.id(),
            menu.parentId(),
            menu.title(),
            menu.name(),
            menu.url(),
            menu.component(),
            menu.type(),
            menu.icon(),
            menu.orderNum(),
            menu.status(),
            new ArrayList<>());
    }

    public MenuDisplay withChildren(Long parentId, List<MenuDisplay> newChildren) {
        return new MenuDisplay(
            id,
            parentId,
            title,
            name,
            url,
            component,
            type,
            icon,
            orderNum,
            status,
            newChildren);
    }
}
