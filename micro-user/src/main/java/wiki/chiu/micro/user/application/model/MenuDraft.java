package wiki.chiu.micro.user.application.model;

import wiki.chiu.micro.user.domain.Menu;

/**
 * A menu entry as submitted by a client, before it is merged with the stored state.
 */
public record MenuDraft(
    Long id,
    Long parentId,
    String title,
    String name,
    String url,
    String component,
    String icon,
    Integer orderNum,
    Integer type,
    Integer status) {

    public Menu mergeInto(Menu dealMenu) {
        return new Menu(
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
            dealMenu.created(),
            dealMenu.updated());
    }
}
