package wiki.chiu.micro.user.api;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

public interface UserHttpService {

  @PatchExchange("/user/{userId}/password-lock")
  Result<Void> lockAfterPasswordFailures(@PathVariable Long userId);

  @PostExchange("/user/role")
  Result<List<RoleEntityRpcVo>> findByRoleCodeInAndStatus(
      @RequestBody List<String> roles, @RequestParam Integer status);

  @PostExchange("/user/login/time")
  Result<Void> updateLoginTime(@RequestParam String username);

  @GetExchange("/user/email")
  Result<UserEntityRpcVo> findByEmail(@RequestParam String email);

  @GetExchange("/user/phone")
  Result<UserEntityRpcVo> findByPhone(@RequestParam String phone);

  @GetExchange("/user/access/{userId}")
  Result<UserAccessRpcVo> findUserAccess(@PathVariable Long userId);

  @GetExchange("/role/authorizations")
  Result<List<RoleAuthorizationRpcVo>> findAllRoleAuthorizations();

  @PostExchange("/role/authorizations")
  Result<List<RoleAuthorizationRpcVo>> findRoleAuthorizations(@RequestBody List<Long> roleIds);

  @GetExchange("/user/{userId}")
  Result<UserEntityRpcVo> findById(@PathVariable Long userId);

  @GetExchange("/user/login/query")
  Result<UserEntityRpcVo> findByUsernameOrEmailOrPhone(@RequestParam String username);
}
