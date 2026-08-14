package wiki.chiu.micro.user.api.vo;

import java.util.List;

public record UserAccessRpcVo(Long userId, boolean exists, Integer status, List<Long> roleIds) {

  public UserAccessRpcVo {
    roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
  }

  public static UserAccessRpcVo missing(Long userId) {
    return new UserAccessRpcVo(userId, false, null, List.of());
  }
}
