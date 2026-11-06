package wiki.chiu.micro.auth.application.port.out;

import java.util.List;

import wiki.chiu.micro.auth.application.model.Authority;
import wiki.chiu.micro.auth.application.model.RoleAuthorization;
import wiki.chiu.micro.auth.application.model.UserAccess;

/** Authoritative reads for issuing bounded collaboration leases. */
public interface FreshAuthorizationDirectory {
    UserAccess findUserAccess(Long userId);
    List<Authority> freshAuthorities();
    List<RoleAuthorization> freshRoleAuthorizations(List<Long> roleIds);
}
