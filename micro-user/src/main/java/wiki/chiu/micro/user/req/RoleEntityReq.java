package wiki.chiu.micro.user.req;

import java.util.List;
import java.util.Optional;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:57 pm
 */
public record RoleEntityReq(
    Optional<Long> id,
    String name,
    String code,
    String remark,
    Integer status,
    List<DataPermissionEnum> dataPermissions) {

  public RoleEntityReq {
    dataPermissions = dataPermissions == null ? List.of() : List.copyOf(dataPermissions);
  }
}
