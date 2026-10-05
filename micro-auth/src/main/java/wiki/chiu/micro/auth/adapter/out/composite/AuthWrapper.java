package wiki.chiu.micro.auth.adapter.out.composite;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.adapter.out.http.UserRpcMapper;
import wiki.chiu.micro.auth.application.model.Authority;
import wiki.chiu.micro.auth.application.model.Menu;
import wiki.chiu.micro.auth.application.model.RoleAuthorization;
import wiki.chiu.micro.auth.application.model.UserAccess;
import wiki.chiu.micro.auth.application.port.out.AuthorizationDirectory;
import wiki.chiu.micro.auth.domain.AuthCacheDescriptors;
import wiki.chiu.micro.cache.annotation.Cache;

@Component
public class AuthWrapper implements AuthorizationDirectory {

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    public AuthWrapper(UserHttpServiceWrapper userHttpServiceWrapper) {
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    @Cache(
        namespace = AuthCacheDescriptors.USER_ACCESS_NAMESPACE,
        version = AuthCacheDescriptors.VERSION)
    @Override
    public UserAccess getUserAccess(Long userId) {
        return userHttpServiceWrapper.findUserAccess(userId);
    }

    @Cache(
        namespace = AuthCacheDescriptors.ROLE_AUTHORIZATION_NAMESPACE,
        version = AuthCacheDescriptors.VERSION)
    @Override
    public List<RoleAuthorization> getAllRoleAuthorizations() {
        return UserRpcMapper.toRoleAuthorizations(userHttpServiceWrapper.findAllRoleAuthorizations());
    }

    @Cache(
        namespace = AuthCacheDescriptors.ROLE_NAVIGATION_NAMESPACE,
        version = AuthCacheDescriptors.VERSION)
    @Override
    public List<Menu> getCurrentUserNav(String rawRole) {
        return UserRpcMapper.toMenus(userHttpServiceWrapper.getCurrentUserNav(rawRole));
    }

    @Cache(
        namespace = AuthCacheDescriptors.SYSTEM_AUTHORITIES_NAMESPACE,
        version = AuthCacheDescriptors.VERSION)
    @Override
    public List<Authority> getAllSystemAuthorities() {
        return UserRpcMapper.toAuthorities(userHttpServiceWrapper.getSystemAuthorities());
    }
}
