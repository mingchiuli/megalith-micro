package wiki.chiu.micro.user.application.model;

import java.util.List;

import wiki.chiu.micro.user.domain.Menu;

/**
 * A menu entry with its children.
 */
public record MenuNode(Menu menu, List<MenuNode> children) {

    public MenuNode {
        children = List.copyOf(children);
    }
}
