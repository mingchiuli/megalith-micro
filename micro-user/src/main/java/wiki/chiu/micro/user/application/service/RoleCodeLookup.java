package wiki.chiu.micro.user.application.service;

import java.util.List;

import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.UserRole;

/**
 * Resolves the codes of the roles that are enabled for a user.
 */
public class RoleCodeLookup {

    private final UserRoleReader userRoles;

    private final RoleReader roles;

    public RoleCodeLookup(UserRoleReader userRoles, RoleReader roles) {
        this.userRoles = userRoles;
        this.roles = roles;
    }

    public List<String> codesOf(Long userId) {
        List<Long> roleIds =
            userRoles.findByUserId(userId).stream().map(UserRole::roleId).toList();
        return roles.findAllById(roleIds).stream()
            .filter(item -> StatusEnum.NORMAL.getCode().equals(item.status()))
            .map(Role::code)
            .toList();
    }
}
