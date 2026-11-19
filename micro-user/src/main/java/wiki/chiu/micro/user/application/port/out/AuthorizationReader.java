package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.application.model.RoleAuthorization;
import wiki.chiu.micro.user.application.model.UserAccess;

public interface AuthorizationReader {

    UserAccess findUserAccess(Long userId);

    List<RoleAuthorization> findRoleAuthorizations(List<Long> roleIds);

    List<RoleAuthorization> findAllRoleAuthorizations();
}
