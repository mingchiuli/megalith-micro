package wiki.chiu.micro.user.application.model;

import java.util.List;

/**
 * A menu entry offered to one role, flagged with whether the role already selects it.
 */
public record MenuSelection(
    Long menuId, String title, boolean selected, List<MenuSelection> children) {

    public MenuSelection {
        children = List.copyOf(children);
    }
}
