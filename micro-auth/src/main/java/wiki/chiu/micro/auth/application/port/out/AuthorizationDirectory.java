package wiki.chiu.micro.auth.application.port.out;

import java.util.List;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

public interface AuthorizationDirectory {

  UserAccessRpcVo getUserAccess(Long userId);

  List<RoleAuthorizationRpcVo> getAllRoleAuthorizations();

  List<MenuDto> getCurrentUserNav(String role);

  List<AuthorityRpcVo> getAllSystemAuthorities();
}
