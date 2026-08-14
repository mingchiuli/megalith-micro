package wiki.chiu.micro.auth.rpc;

import java.util.List;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.service.port.UserDirectory;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.user.api.AuthorityHttpService;
import wiki.chiu.micro.user.api.MenuHttpService;
import wiki.chiu.micro.user.api.UserHttpService;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

@Component
public class UserHttpServiceWrapper implements UserDirectory {

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

  public void lockAfterPasswordFailures(Long userId) {
    RemoteResult.requireSuccess(() -> userHttpService.lockAfterPasswordFailures(userId));
  }

  public List<RoleEntityRpcVo> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
    return RemoteResult.requireSuccess(
        () -> userHttpService.findByRoleCodeInAndStatus(roles, status));
  }

  public void updateLoginTime(String username) {
    RemoteResult.requireSuccess(() -> userHttpService.updateLoginTime(username));
  }

  @Override
  public void findByEmail(String loginEmail) {
    RemoteResult.requireSuccess(() -> userHttpService.findByEmail(loginEmail));
  }

  @Override
  public void findByPhone(String loginSMS) {
    RemoteResult.requireSuccess(() -> userHttpService.findByPhone(loginSMS));
  }

  @Override
  public UserAccessRpcVo findUserAccess(Long userId) {
    return RemoteResult.requireSuccess(() -> userHttpService.findUserAccess(userId));
  }

  @Override
  public RoleAuthorizationRpcVo findRoleAuthorization(Long roleId) {
    return RemoteResult.requireSuccess(() -> userHttpService.findRoleAuthorization(roleId));
  }

  public List<RoleAuthorizationRpcVo> findRoleAuthorizations(List<Long> roleIds) {
    return RemoteResult.requireSuccess(() -> userHttpService.findRoleAuthorizations(roleIds));
  }

  @Override
  public UserEntityRpcVo findById(Long userId) {
    return RemoteResult.requireSuccess(() -> userHttpService.findById(userId));
  }

  public UserEntityRpcVo findByUsernameOrEmailOrPhone(String username) {
    return RemoteResult.requireSuccess(
        () -> userHttpService.findByUsernameOrEmailOrPhone(username));
  }

  public List<MenuRpcVo> getCurrentUserNav(String rawRole) {
    return RemoteResult.requireSuccess(() -> menuHttpService.getCurrentUserNav(rawRole));
  }

  public List<AuthorityRpcVo> getSystemAuthorities() {
    return RemoteResult.requireSuccess(authorityHttpService::getAuthorities);
  }
}
