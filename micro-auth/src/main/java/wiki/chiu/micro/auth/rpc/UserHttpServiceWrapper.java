package wiki.chiu.micro.auth.rpc;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.rpc.AuthorityHttpService;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.common.vo.MenuRpcVo;
import wiki.chiu.micro.common.vo.RoleEntityRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;

@Component
public class UserHttpServiceWrapper {

  private final UserHttpService userHttpService;

  private final MenuHttpService menuHttpService;

  private final AuthorityHttpService authorityHttpService;

  public UserHttpServiceWrapper(
      UserHttpService userHttpService,
      MenuHttpService menuHttpService,
      AuthorityHttpService authorityHttpService) {
    this.userHttpService = userHttpService;
    this.menuHttpService = menuHttpService;
    this.authorityHttpService = authorityHttpService;
  }

  public void changeUserStatusByUsername(String username, Integer status) {
    userHttpService.changeUserStatusByUsername(username, status);
  }

  public List<RoleEntityRpcVo> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
    return RemoteResult.requireSuccess(
        () -> userHttpService.findByRoleCodeInAndStatus(roles, status));
  }

  public void updateLoginTime(String username) {
    RemoteResult.requireSuccess(() -> userHttpService.updateLoginTime(username));
  }

  public void findByEmail(String loginEmail) {
    RemoteResult.requireSuccess(() -> userHttpService.findByEmail(loginEmail));
  }

  public void findByPhone(String loginSMS) {
    RemoteResult.requireSuccess(() -> userHttpService.findByPhone(loginSMS));
  }

  public List<String> findRoleCodesByUserId(Long userId) {
    return RemoteResult.requireSuccess(() -> userHttpService.findRoleCodesByUserId(userId));
  }

  public UserEntityRpcVo findById(Long userId) {
    return RemoteResult.requireSuccess(() -> userHttpService.findById(userId));
  }

  public UserEntityRpcVo findByUsernameOrEmailOrPhone(String username) {
    return RemoteResult.requireSuccess(
        () -> userHttpService.findByUsernameOrEmailOrPhone(username));
  }

  public Set<String> getAuthoritiesByRoleCode(String rawRole) {
    return RemoteResult.requireSuccess(
        () -> authorityHttpService.getAuthoritiesByRoleCode(rawRole));
  }

  public List<MenuRpcVo> getCurrentUserNav(String rawRole) {
    return RemoteResult.requireSuccess(() -> menuHttpService.getCurrentUserNav(rawRole));
  }

  public List<AuthorityRpcVo> getSystemAuthorities() {
    return RemoteResult.requireSuccess(authorityHttpService::getAuthorities);
  }
}
