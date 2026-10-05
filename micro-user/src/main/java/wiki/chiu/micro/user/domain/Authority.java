package wiki.chiu.micro.user.domain;

import java.time.LocalDateTime;

/**
 * A routable authority, without any persistence, delivery, or messaging dependency.
 */
public record Authority(
    Long id,
    String code,
    String remark,
    String prototype,
    String methodType,
    String routePattern,
    String serviceHost,
    Integer servicePort,
    LocalDateTime created,
    LocalDateTime updated,
    Integer type,
    Integer status) {

    /** A transient authority that has not been stored yet. */
    public static Authority blank() {
        return new Authority(
            null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public Authority withId(Long newId) {
        return new Authority(
            newId, code, remark, prototype, methodType, routePattern, serviceHost, servicePort,
            created, updated, type, status);
    }

    public Authority withCreated(LocalDateTime newCreated) {
        return new Authority(
            id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort,
            newCreated, updated, type, status);
    }
}
