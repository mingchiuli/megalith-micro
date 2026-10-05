package wiki.chiu.micro.user.domain;

import java.time.LocalDateTime;

/**
 * The link between an account and a role.
 */
public record UserRole(Long id, Long userId, Long roleId, LocalDateTime created, LocalDateTime updated) {

    public UserRole onUser(Long newUserId) {
        return new UserRole(id, newUserId, roleId, created, updated);
    }
}
