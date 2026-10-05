package wiki.chiu.micro.user.application.model;

import java.util.List;

import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.RoleDataPermission;
import wiki.chiu.micro.user.domain.UserRole;

/**
 * The rows the role export delivers: the roles with their user assignments and data permissions.
 */
public record RoleExport(
    List<Role> roles, List<UserRole> userRoles, List<RoleDataPermission> dataPermissions) {
}
