package wiki.chiu.micro.user.api.vo;

import java.util.List;
import java.util.Set;

import wiki.chiu.micro.common.lang.DataPermissionEnum;

public record RoleAuthorizationRpcVo(
    Long roleId,
    boolean exists,
    String code,
    Integer status,
    Set<String> authorityCodes,
    List<DataPermissionEnum> dataPermissions) {

    public RoleAuthorizationRpcVo {
        authorityCodes = authorityCodes == null ? Set.of() : Set.copyOf(authorityCodes);
        dataPermissions = dataPermissions == null ? List.of() : List.copyOf(dataPermissions);
    }

    public static RoleAuthorizationRpcVo missing(Long roleId) {
        return new RoleAuthorizationRpcVo(roleId, false, null, null, Set.of(), List.of());
    }
}
