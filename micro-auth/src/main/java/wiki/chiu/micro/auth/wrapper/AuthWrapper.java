package wiki.chiu.micro.auth.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.cache.AuthCacheDescriptors;
import wiki.chiu.micro.auth.convertor.MenuDtoConvertor;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

@Component
public class AuthWrapper {

  private final UserHttpServiceWrapper userHttpServiceWrapper;

  public AuthWrapper(UserHttpServiceWrapper userHttpServiceWrapper) {
    this.userHttpServiceWrapper = userHttpServiceWrapper;
  }

  @Cache(
      namespace = AuthCacheDescriptors.USER_ACCESS_NAMESPACE,
      version = AuthCacheDescriptors.VERSION)
  public UserAccessRpcVo getUserAccess(Long userId) {
    return userHttpServiceWrapper.findUserAccess(userId);
  }

  @Cache(
      namespace = AuthCacheDescriptors.ROLE_AUTHORIZATION_NAMESPACE,
      version = AuthCacheDescriptors.VERSION)
  public List<RoleAuthorizationRpcVo> getAllRoleAuthorizations() {
    return userHttpServiceWrapper.findAllRoleAuthorizations();
  }

  @Cache(
      namespace = AuthCacheDescriptors.ROLE_NAVIGATION_NAMESPACE,
      version = AuthCacheDescriptors.VERSION)
  public List<MenuDto> getCurrentUserNav(String rawRole) {
    List<MenuRpcVo> dto = userHttpServiceWrapper.getCurrentUserNav(rawRole);
    return MenuDtoConvertor.convert(dto);
  }

  @Cache(
      namespace = AuthCacheDescriptors.SYSTEM_AUTHORITIES_NAMESPACE,
      version = AuthCacheDescriptors.VERSION)
  public List<AuthorityRpcVo> getAllSystemAuthorities() {
    return userHttpServiceWrapper.getSystemAuthorities();
  }
}
