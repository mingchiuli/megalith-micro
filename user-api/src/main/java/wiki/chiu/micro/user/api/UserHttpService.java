package wiki.chiu.micro.user.api;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.api.vo.UserAuthContextRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

public interface UserHttpService {

  @PatchExchange("/user/status")
  Result<Void> changeUserStatusByUsername(
      @RequestParam String username, @RequestParam Integer status);

  @PostExchange("/user/role")
  Result<List<RoleEntityRpcVo>> findByRoleCodeInAndStatus(
      @RequestBody List<String> roles, @RequestParam Integer status);

  @PostExchange("/user/login/time")
  Result<Void> updateLoginTime(@RequestParam String username);

  @GetExchange("/user/email")
  Result<UserEntityRpcVo> findByEmail(@RequestParam String email);

  @GetExchange("/user/phone")
  Result<UserEntityRpcVo> findByPhone(@RequestParam String phone);

  @GetExchange("/user/auth-context/{userId}")
  Result<UserAuthContextRpcVo> findAuthContext(@PathVariable Long userId);

  @GetExchange("/user/{userId}")
  Result<UserEntityRpcVo> findById(@PathVariable Long userId);

  @GetExchange("/user/login/query")
  Result<UserEntityRpcVo> findByUsernameOrEmailOrPhone(@RequestParam String username);
}
