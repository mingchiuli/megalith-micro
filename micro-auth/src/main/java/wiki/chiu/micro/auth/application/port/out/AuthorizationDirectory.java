package wiki.chiu.micro.auth.application.port.out;

import java.util.List;

import wiki.chiu.micro.auth.application.model.Authority;
import wiki.chiu.micro.auth.application.model.Menu;
import wiki.chiu.micro.auth.application.model.RoleAuthorization;
import wiki.chiu.micro.auth.application.model.UserAccess;

public interface AuthorizationDirectory {

    UserAccess getUserAccess(Long userId);

    List<RoleAuthorization> getAllRoleAuthorizations();

    List<Menu> getCurrentUserNav(String role);

    List<Authority> getAllSystemAuthorities();
}
