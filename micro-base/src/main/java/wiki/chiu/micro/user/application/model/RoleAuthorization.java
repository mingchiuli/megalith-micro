package wiki.chiu.micro.user.application.model;

import java.util.List;
import java.util.Set;

import wiki.chiu.micro.common.enums.DataPermissionEnum;

/**
 * What a role grants, as resolved for the authorization queries.
 */
public record RoleAuthorization(
    Long roleId,
    boolean exists,
    String code,
    Integer status,
    Set<String> authorityCodes,
    List<DataPermissionEnum> dataPermissions) {

    public RoleAuthorization {
        authorityCodes = authorityCodes == null ? Set.of() : Set.copyOf(authorityCodes);
        dataPermissions = dataPermissions == null ? List.of() : List.copyOf(dataPermissions);
    }

    public static RoleAuthorization missing(Long roleId) {
        return new RoleAuthorization(roleId, false, null, null, Set.of(), List.of());
    }
}
