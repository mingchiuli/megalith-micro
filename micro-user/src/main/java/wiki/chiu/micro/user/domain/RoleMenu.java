package wiki.chiu.micro.user.domain;

import java.time.LocalDateTime;

/**
 * The link between a role and a menu entry.
 */
public record RoleMenu(Long id, Long roleId, Long menuId, LocalDateTime created, LocalDateTime updated) {

    public RoleMenu onRole(Long newRoleId) {
        return new RoleMenu(id, newRoleId, menuId, created, updated);
    }
}
