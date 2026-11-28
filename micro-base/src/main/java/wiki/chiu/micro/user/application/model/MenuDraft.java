package wiki.chiu.micro.user.application.model;

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

}
