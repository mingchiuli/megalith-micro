package wiki.chiu.micro.user.api.vo;

import java.util.List;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

public record UserAuthContextRpcVo(
    Long userId, Integer status, List<String> roles, List<DataPermissionEnum> dataPermissions) {

  public UserAuthContextRpcVo {
    dataPermissions = dataPermissions == null ? List.of() : List.copyOf(dataPermissions);
  }

  public UserAuthContextRpcVo(Long userId, Integer status, List<String> roles) {
    this(userId, status, roles, List.of());
  }
}
