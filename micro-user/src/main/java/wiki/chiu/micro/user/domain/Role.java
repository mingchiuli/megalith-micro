package wiki.chiu.micro.user.domain;

import java.time.LocalDateTime;

/**
 * A role, without any persistence, delivery, or messaging dependency.
 */
public record Role(
    Long id,
    String name,
    String code,
    String remark,
    LocalDateTime created,
    LocalDateTime updated,
    Integer status) {

    /** A transient role that has not been stored yet. */
    public static Role blank() {
        return new Role(null, null, null, null, null, null, null);
    }

    public Role withUpdated(java.time.LocalDateTime newUpdated) {
        return new Role(id, name, code, remark, created, newUpdated, status);
    }

    public Role withId(Long newId) {
        return new Role(newId, name, code, remark, created, updated, status);
    }

    public Role withName(String newName) {
        return new Role(id, newName, code, remark, created, updated, status);
    }

    public Role withCode(String newCode) {
        return new Role(id, name, newCode, remark, created, updated, status);
    }

    public Role withRemark(String newRemark) {
        return new Role(id, name, code, newRemark, created, updated, status);
    }

    public Role withCreated(LocalDateTime newCreated) {
        return new Role(id, name, code, remark, newCreated, updated, status);
    }

    public Role withStatus(Integer newStatus) {
        return new Role(id, name, code, remark, created, updated, newStatus);
    }
}
