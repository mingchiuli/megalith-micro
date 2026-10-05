package wiki.chiu.micro.user.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.application.model.RoleAuthorization;
import wiki.chiu.micro.user.application.model.UserAccess;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.application.model.MenuAuthorityView;
import wiki.chiu.micro.user.application.model.MenuNode;
import wiki.chiu.micro.user.application.model.MenuSelection;
import wiki.chiu.micro.user.application.model.RoleView;
import wiki.chiu.micro.user.application.model.UserView;
import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.Menu;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.User;

/**
 * Renders the user use-case results as the HTTP bodies and published RPC payloads callers expect.
 */
public final class UserViewMapper {

    private UserViewMapper() {
    }

    public static AuthorityVo toVo(Authority authority) {
        return AuthorityVo.builder()
            .id(authority.id())
            .code(authority.code())
            .prototype(authority.prototype())
            .methodType(authority.methodType())
            .serviceHost(authority.serviceHost())
            .servicePort(authority.servicePort())
            .routePattern(authority.routePattern())
            .created(authority.created())
            .updated(authority.updated())
            .type(authority.type())
            .status(authority.status())
            .remark(authority.remark())
            .build();
    }

    public static List<AuthorityVo> toAuthorityVos(List<Authority> authorities) {
        return authorities.stream().map(UserViewMapper::toVo).toList();
    }

    public static MenuEntityVo toVo(Menu menu) {
        return MenuEntityVo.builder()
            .id(menu.id())
            .url(menu.url())
            .title(menu.title())
            .type(menu.type())
            .name(menu.name())
            .component(menu.component())
            .orderNum(menu.orderNum())
            .parentId(menu.parentId())
            .icon(menu.icon())
            .status(menu.status())
            .build();
    }

    public static MenuDisplayVo toVo(MenuNode node) {
        return MenuDisplayVo.builder()
            .id(node.menu().id())
            .parentId(node.menu().parentId())
            .icon(node.menu().icon())
            .url(node.menu().url())
            .updated(node.menu().updated())
            .created(node.menu().created())
            .title(node.menu().title())
            .name(node.menu().name())
            .component(node.menu().component())
            .type(node.menu().type())
            .orderNum(node.menu().orderNum())
            .status(node.menu().status())
            .children(node.children().stream().map(UserViewMapper::toVo).toList())
            .build();
    }

    public static List<MenuDisplayVo> toMenuDisplayVos(List<MenuNode> nodes) {
        return nodes.stream().map(UserViewMapper::toVo).toList();
    }

    public static RoleEntityVo toVo(RoleView view) {
        return RoleEntityVo.builder()
            .id(view.role().id())
            .name(view.role().name())
            .code(view.role().code())
            .remark(view.role().remark())
            .created(view.role().created())
            .updated(view.role().updated())
            .status(view.role().status())
            .dataPermissions(view.dataPermissions())
            .build();
    }

    public static List<RoleEntityVo> toRoleVos(List<Role> roles) {
        return roles.stream()
            .map(role -> new RoleView(role, List.of()))
            .map(UserViewMapper::toVo)
            .toList();
    }

    public static PageAdapter<RoleEntityVo> toRoleVos(PageAdapter<RoleView> page) {
        return PageAdapter.<RoleEntityVo>builder()
            .content(page.content().stream().map(UserViewMapper::toVo).toList())
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }

    public static RoleMenuVo toVo(MenuSelection selection) {
        return RoleMenuVo.builder()
            .menuId(selection.menuId())
            .title(selection.title())
            .check(selection.selected())
            .children(selection.children().stream().map(UserViewMapper::toVo).toList())
            .build();
    }

    public static List<RoleMenuVo> toRoleMenuVos(List<MenuSelection> selections) {
        return selections.stream().map(UserViewMapper::toVo).toList();
    }

    public static MenuAuthorityVo toVo(MenuAuthorityView view) {
        return MenuAuthorityVo.builder()
            .authorityId(view.authorityId())
            .code(view.code())
            .check(view.selected())
            .build();
    }

    public static List<MenuAuthorityVo> toMenuAuthorityVos(List<MenuAuthorityView> views) {
        return views.stream().map(UserViewMapper::toVo).toList();
    }

    public static UserEntityVo toVo(UserView view) {
        User user = view.user();
        return UserEntityVo.builder()
            .id(user.id())
            .username(user.username())
            .nickname(user.nickname())
            .avatar(user.avatar())
            .email(user.email())
            .phone(user.phone())
            .status(user.status())
            .created(user.created())
            .updated(user.updated())
            .lastLogin(user.lastLogin())
            .roles(view.roles())
            .build();
    }

    public static PageAdapter<UserEntityVo> toUserVos(PageAdapter<UserView> page) {
        return PageAdapter.<UserEntityVo>builder()
            .content(page.content().stream().map(UserViewMapper::toVo).toList())
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }

    public static UserAccessRpcVo toRpc(UserAccess access) {
        return new UserAccessRpcVo(
            access.userId(), access.exists(), access.status(), access.roleIds());
    }

    public static RoleAuthorizationRpcVo toRpc(RoleAuthorization authorization) {
        return new RoleAuthorizationRpcVo(
            authorization.roleId(),
            authorization.exists(),
            authorization.code(),
            authorization.status(),
            authorization.authorityCodes(),
            authorization.dataPermissions());
    }

    public static List<RoleAuthorizationRpcVo> toRpcs(List<RoleAuthorization> authorizations) {
        return authorizations.stream().map(UserViewMapper::toRpc).toList();
    }

    public static AuthorityRpcVo toRpc(Authority authority) {
        return AuthorityRpcVo.builder()
            .id(authority.id())
            .code(authority.code())
            .prototype(authority.prototype())
            .methodType(authority.methodType())
            .serviceHost(authority.serviceHost())
            .servicePort(authority.servicePort())
            .routePattern(authority.routePattern())
            .type(authority.type())
            .status(authority.status())
            .remark(authority.remark())
            .build();
    }

    public static List<AuthorityRpcVo> toAuthorityRpcs(List<Authority> authorities) {
        return authorities.stream().map(UserViewMapper::toRpc).toList();
    }

    public static UserEntityRpcVo toRpc(User user) {
        return UserEntityRpcVo.builder()
            .id(user.id())
            .username(user.username())
            .nickname(user.nickname())
            .avatar(user.avatar())
            .email(user.email())
            .phone(user.phone())
            .password(user.password())
            .status(user.status())
            .created(user.created())
            .updated(user.updated())
            .lastLogin(user.lastLogin())
            .build();
    }

    public static RoleEntityRpcVo toRpc(Role role) {
        return RoleEntityRpcVo.builder()
            .id(role.id())
            .name(role.name())
            .code(role.code())
            .remark(role.remark())
            .status(role.status())
            .created(role.created())
            .updated(role.updated())
            .build();
    }

    public static List<RoleEntityRpcVo> toRoleRpcs(List<Role> roles) {
        return roles.stream().map(UserViewMapper::toRpc).toList();
    }

    public static MenuRpcVo toRpc(Menu menu) {
        return MenuRpcVo.builder()
            .id(menu.id())
            .parentId(menu.parentId())
            .icon(menu.icon())
            .url(menu.url())
            .title(menu.title())
            .name(menu.name())
            .component(menu.component())
            .type(menu.type())
            .orderNum(menu.orderNum())
            .status(menu.status())
            .build();
    }

    public static List<MenuRpcVo> toMenuRpcs(List<Menu> menus) {
        return menus.stream().map(UserViewMapper::toRpc).toList();
    }
}
