package wiki.chiu.micro.user.application.model;

import java.util.List;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.user.domain.Role;

/**
 * A role with the data permissions granted to it.
 */
public record RoleView(Role role, List<DataPermissionEnum> dataPermissions) {

    public RoleView {
        dataPermissions = List.copyOf(dataPermissions);
    }
}
