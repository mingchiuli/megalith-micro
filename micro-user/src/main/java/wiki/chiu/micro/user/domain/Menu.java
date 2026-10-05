package wiki.chiu.micro.user.domain;

import java.time.LocalDateTime;

/**
 * A navigation entry, without any persistence, delivery, or messaging dependency.
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
    Integer status,
    LocalDateTime created,
    LocalDateTime updated) {

    /** A transient menu entry that has not been stored yet. */
    public static Menu blank() {
        return new Menu(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public Menu withId(Long newId) {
        return new Menu(
            newId, parentId, title, name, url, component, type, icon, orderNum, status, created,
            updated);
    }

    public Menu withUpdated(LocalDateTime newUpdated) {
        return new Menu(
            id, parentId, title, name, url, component, type, icon, orderNum, status, created,
            newUpdated);
    }

    public Menu withStatus(Integer newStatus) {
        return new Menu(
            id, parentId, title, name, url, component, type, icon, orderNum, newStatus, created,
            updated);
    }

    public Menu withCreated(LocalDateTime newCreated) {
        return new Menu(
            id, parentId, title, name, url, component, type, icon, orderNum, status, newCreated,
            updated);
    }
}
