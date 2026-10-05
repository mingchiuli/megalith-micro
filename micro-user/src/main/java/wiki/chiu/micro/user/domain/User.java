package wiki.chiu.micro.user.domain;

import java.time.LocalDateTime;

/**
 * A registered account, without any persistence, delivery, or messaging dependency.
 */
public record User(
    Long id,
    String username,
    String nickname,
    String avatar,
    String email,
    String phone,
    String password,
    Integer status,
    LocalDateTime passwordLockedUntil,
    LocalDateTime created,
    LocalDateTime updated,
    LocalDateTime lastLogin) {

    /** A transient account that has not been stored yet. */
    public static User blank() {
        return new User(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public User withUpdated(java.time.LocalDateTime newUpdated) {
        return new User(
            id, username, nickname, avatar, email, phone, password, status, passwordLockedUntil,
            created, newUpdated, lastLogin);
    }

    public User withId(Long newId) {
        return new User(
            newId, username, nickname, avatar, email, phone, password, status, passwordLockedUntil,
            created, updated, lastLogin);
    }

    public User withStatus(Integer newStatus) {
        return new User(
            id, username, nickname, avatar, email, phone, password, newStatus, passwordLockedUntil,
            created, updated, lastLogin);
    }

    public User withPasswordLockedUntil(LocalDateTime lockedUntil) {
        return new User(
            id, username, nickname, avatar, email, phone, password, status, lockedUntil, created,
            updated, lastLogin);
    }
}
