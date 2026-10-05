package wiki.chiu.micro.user.application.service;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.user.application.model.RoleAuthorization;
import wiki.chiu.micro.user.application.model.UserAccess;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.MenuAuthority;
import wiki.chiu.micro.user.domain.RoleDataPermission;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.RoleMenu;
import wiki.chiu.micro.user.domain.UserRole;

public class AuthorizationQueryService {

    private final UserReader users;
    private final UserRoleReader userRoles;
    private final RoleReader roles;
    private final RoleMenuReader roleMenus;
    private final MenuAuthorityReader menuAuthorities;
    private final AuthorityReader authorities;
    private final RoleDataPermissionReader dataPermissions;

    public AuthorizationQueryService(
        UserReader users,
        UserRoleReader userRoles,
        RoleReader roles,
        RoleMenuReader roleMenus,
        MenuAuthorityReader menuAuthorities,
        AuthorityReader authorities,
        RoleDataPermissionReader dataPermissions) {
        this.users = users;
        this.userRoles = userRoles;
        this.roles = roles;
        this.roleMenus = roleMenus;
        this.menuAuthorities = menuAuthorities;
        this.authorities = authorities;
        this.dataPermissions = dataPermissions;
    }

    public UserAccess findUserAccess(Long userId) {
        return users
            .findById(userId)
            .map(
                user ->
                    new UserAccess(
                        user.id(),
                        true,
                        user.status(),
                        userRoles.findByUserId(userId).stream()
                            .map(UserRole::roleId)
                            .distinct()
                            .toList()))
            .orElseGet(() -> UserAccess.missing(userId));
    }

    public List<RoleAuthorization> findRoleAuthorizations(List<Long> roleIds) {
        List<Long> distinctRoleIds = roleIds.stream().distinct().toList();
        if (distinctRoleIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Role> rolesById =
            roles.findAllById(distinctRoleIds).stream()
                .collect(Collectors.toMap(Role::id, Function.identity()));
        return findRoleAuthorizationsInternal(distinctRoleIds, rolesById);
    }

    public List<RoleAuthorization> findAllRoleAuthorizations() {
        List<Role> allRoles = roles.findAll();
        List<Long> distinctRoleIds = allRoles.stream().map(Role::id).distinct().toList();
        if (distinctRoleIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Role> rolesById =
            allRoles.stream().collect(Collectors.toMap(Role::id, Function.identity()));
        return findRoleAuthorizationsInternal(distinctRoleIds, rolesById);
    }

    private List<RoleAuthorization> findRoleAuthorizationsInternal(
        List<Long> distinctRoleIds, Map<Long, Role> rolesById) {
        List<Long> existingRoleIds = distinctRoleIds.stream().filter(rolesById::containsKey).toList();
        if (existingRoleIds.isEmpty()) {
            return distinctRoleIds.stream().map(RoleAuthorization::missing).toList();
        }

        List<RoleMenu> roleMenuEntities = roleMenus.findByRoleIdIn(existingRoleIds);
        Map<Long, List<Long>> menuIdsByRole =
            roleMenuEntities.stream()
                .collect(
                    Collectors.groupingBy(
                        RoleMenu::roleId,
                        Collectors.mapping(RoleMenu::menuId, Collectors.toList())));
        List<Long> menuIds =
            roleMenuEntities.stream().map(RoleMenu::menuId).distinct().toList();
        List<MenuAuthority> menuAuthorityEntities =
            menuIds.isEmpty() ? List.of() : menuAuthorities.findByMenuIdIn(menuIds);
        Map<Long, List<Long>> authorityIdsByMenu =
            menuAuthorityEntities.stream()
                .collect(
                    Collectors.groupingBy(
                        MenuAuthority::menuId,
                        Collectors.mapping(MenuAuthority::authorityId, Collectors.toList())));
        List<Long> authorityIds =
            menuAuthorityEntities.stream().map(MenuAuthority::authorityId).distinct().toList();
        Map<Long, Authority> authoritiesById =
            authorityIds.isEmpty()
                ? Map.of()
                : authorities.findByIdInAndStatus(authorityIds, StatusEnum.NORMAL.getCode()).stream()
                  .collect(Collectors.toMap(Authority::id, Function.identity()));
        Map<Long, List<DataPermissionEnum>> permissionsByRole =
            dataPermissions.findByRoleIdIn(existingRoleIds).stream()
                .collect(
                    Collectors.groupingBy(
                        RoleDataPermission::roleId,
                        Collectors.collectingAndThen(
                            Collectors.mapping(
                                RoleDataPermission::permission, Collectors.toList()),
                            permissions -> permissions.stream().distinct().sorted().toList())));

        return distinctRoleIds.stream()
            .map(
                roleId ->
                    toAuthorization(
                        roleId,
                        rolesById,
                        menuIdsByRole,
                        authorityIdsByMenu,
                        authoritiesById,
                        permissionsByRole))
            .toList();
    }

    private RoleAuthorization toAuthorization(
        Long roleId,
        Map<Long, Role> rolesById,
        Map<Long, List<Long>> menuIdsByRole,
        Map<Long, List<Long>> authorityIdsByMenu,
        Map<Long, Authority> authoritiesById,
        Map<Long, List<DataPermissionEnum>> permissionsByRole) {
        Role role = rolesById.get(roleId);
        if (role == null) {
            return RoleAuthorization.missing(roleId);
        }
        Set<String> authorityCodes =
            menuIdsByRole.getOrDefault(roleId, List.of()).stream()
                .flatMap(menuId -> authorityIdsByMenu.getOrDefault(menuId, List.of()).stream())
                .map(authoritiesById::get)
                .filter(Objects::nonNull)
                .map(Authority::code)
                .collect(Collectors.toUnmodifiableSet());
        return new RoleAuthorization(
            role.id(),
            true,
            role.code(),
            role.status(),
            authorityCodes,
            permissionsByRole.getOrDefault(roleId, List.of()));
    }
}
