package wiki.chiu.micro.user.domain;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.enums.DataPermissionEnum;

/**
 * A data permission granted to a role.
 */
public record RoleDataPermission(
    Long id,
    Long roleId,
    DataPermissionEnum permission,
    LocalDateTime created,
    LocalDateTime updated) {

    public RoleDataPermission onRole(Long newRoleId) {
        return new RoleDataPermission(id, newRoleId, permission, created, updated);
    }
}
