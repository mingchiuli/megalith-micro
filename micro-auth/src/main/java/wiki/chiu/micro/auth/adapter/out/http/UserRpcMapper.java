package wiki.chiu.micro.auth.adapter.out.http;

import java.util.List;

import wiki.chiu.micro.auth.application.model.Authority;
import wiki.chiu.micro.auth.application.model.Menu;
import wiki.chiu.micro.auth.application.model.RoleAuthorization;
import wiki.chiu.micro.auth.application.model.UserAccess;
import wiki.chiu.micro.auth.application.model.UserAccount;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

/**
 * Turns the user service payloads into this service's own model.
 */
public final class UserRpcMapper {

    private UserRpcMapper() {
    }

    public static UserAccount toAccount(UserEntityRpcVo user) {
        return new UserAccount(
            user.id(), user.username(), user.password(), user.nickname(), user.avatar(), user.status());
    }

    public static UserAccess toAccess(UserAccessRpcVo access) {
        return new UserAccess(access.userId(), access.exists(), access.status(), access.roleIds());
    }

    public static List<RoleAuthorization> toRoleAuthorizations(List<RoleAuthorizationRpcVo> roles) {
        return roles.stream()
            .map(
                role ->
                    new RoleAuthorization(
                        role.roleId(),
                        role.exists(),
                        role.code(),
                        role.status(),
                        role.authorityCodes(),
                        role.dataPermissions()))
            .toList();
    }

    public static List<Menu> toMenus(List<MenuRpcVo> menus) {
        return menus.stream()
            .map(
                menu ->
                    new Menu(
                        menu.id(),
                        menu.parentId(),
                        menu.title(),
                        menu.name(),
                        menu.url(),
                        menu.component(),
                        menu.type(),
                        menu.icon(),
                        menu.orderNum(),
                        menu.status()))
            .toList();
    }

    public static List<Authority> toAuthorities(List<AuthorityRpcVo> authorities) {
        return authorities.stream()
            .map(
                authority ->
                    new Authority(
                        authority.id(),
                        authority.code(),
                        authority.methodType(),
                        authority.routePattern(),
                        authority.serviceHost(),
                        authority.servicePort(),
                        authority.type()))
            .toList();
    }
}
