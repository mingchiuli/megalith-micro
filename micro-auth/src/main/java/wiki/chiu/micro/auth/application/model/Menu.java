package wiki.chiu.micro.auth.application.model;

/**
 * A menu entry as it arrives from the user service.
 */
public record Menu(
    Long id,
    Long parentId,
    String title,
    String name,
    String url,
    String component,
    Integer type,
    String icon,
    Integer orderNum,
    Integer status) {
}
