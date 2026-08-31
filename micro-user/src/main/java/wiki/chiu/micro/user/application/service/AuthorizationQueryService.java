package wiki.chiu.micro.user.application.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.AuthorityEntity;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.RoleMenuEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;

@Service
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

    public UserAccessRpcVo findUserAccess(Long userId) {
        return users
            .findById(userId)
            .map(
                user ->
                    new UserAccessRpcVo(
                        user.getId(),
                        true,
                        user.getStatus(),
                        userRoles.findByUserId(userId).stream()
                            .map(UserRoleEntity::getRoleId)
                            .distinct()
                            .toList()))
            .orElseGet(() -> UserAccessRpcVo.missing(userId));
    }

    public List<RoleAuthorizationRpcVo> findRoleAuthorizations(List<Long> roleIds) {
        List<Long> distinctRoleIds = roleIds.stream().distinct().toList();
        if (distinctRoleIds.isEmpty()) {
            return List.of();
        }

        Map<Long, RoleEntity> rolesById =
            roles.findAllById(distinctRoleIds).stream()
                .collect(Collectors.toMap(RoleEntity::getId, Function.identity()));
        return findRoleAuthorizationsInternal(distinctRoleIds, rolesById);
    }

    public List<RoleAuthorizationRpcVo> findAllRoleAuthorizations() {
        List<RoleEntity> allRoles = roles.findAll();
        List<Long> distinctRoleIds = allRoles.stream().map(RoleEntity::getId).distinct().toList();
        if (distinctRoleIds.isEmpty()) {
            return List.of();
        }

        Map<Long, RoleEntity> rolesById =
            allRoles.stream().collect(Collectors.toMap(RoleEntity::getId, Function.identity()));
        return findRoleAuthorizationsInternal(distinctRoleIds, rolesById);
    }

    private List<RoleAuthorizationRpcVo> findRoleAuthorizationsInternal(
        List<Long> distinctRoleIds, Map<Long, RoleEntity> rolesById) {
        List<Long> existingRoleIds = distinctRoleIds.stream().filter(rolesById::containsKey).toList();
        if (existingRoleIds.isEmpty()) {
            return distinctRoleIds.stream().map(RoleAuthorizationRpcVo::missing).toList();
        }

        List<RoleMenuEntity> roleMenuEntities = roleMenus.findByRoleIdIn(existingRoleIds);
        Map<Long, List<Long>> menuIdsByRole =
            roleMenuEntities.stream()
                .collect(
                    Collectors.groupingBy(
                        RoleMenuEntity::getRoleId,
                        Collectors.mapping(RoleMenuEntity::getMenuId, Collectors.toList())));
        List<Long> menuIds =
            roleMenuEntities.stream().map(RoleMenuEntity::getMenuId).distinct().toList();
        List<MenuAuthorityEntity> menuAuthorityEntities =
            menuIds.isEmpty() ? List.of() : menuAuthorities.findByMenuIdIn(menuIds);
        Map<Long, List<Long>> authorityIdsByMenu =
            menuAuthorityEntities.stream()
                .collect(
                    Collectors.groupingBy(
                        MenuAuthorityEntity::getMenuId,
                        Collectors.mapping(MenuAuthorityEntity::getAuthorityId, Collectors.toList())));
        List<Long> authorityIds =
            menuAuthorityEntities.stream().map(MenuAuthorityEntity::getAuthorityId).distinct().toList();
        Map<Long, AuthorityEntity> authoritiesById =
            authorityIds.isEmpty()
                ? Map.of()
                : authorities.findByIdInAndStatus(authorityIds, StatusEnum.NORMAL.getCode()).stream()
                  .collect(Collectors.toMap(AuthorityEntity::getId, Function.identity()));
        Map<Long, List<DataPermissionEnum>> permissionsByRole =
            dataPermissions.findByRoleIdIn(existingRoleIds).stream()
                .collect(
                    Collectors.groupingBy(
                        RoleDataPermissionEntity::getRoleId,
                        Collectors.collectingAndThen(
                            Collectors.mapping(
                                RoleDataPermissionEntity::permission, Collectors.toList()),
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

    private RoleAuthorizationRpcVo toAuthorization(
        Long roleId,
        Map<Long, RoleEntity> rolesById,
        Map<Long, List<Long>> menuIdsByRole,
        Map<Long, List<Long>> authorityIdsByMenu,
        Map<Long, AuthorityEntity> authoritiesById,
        Map<Long, List<DataPermissionEnum>> permissionsByRole) {
        RoleEntity role = rolesById.get(roleId);
        if (role == null) {
            return RoleAuthorizationRpcVo.missing(roleId);
        }
        Set<String> authorityCodes =
            menuIdsByRole.getOrDefault(roleId, List.of()).stream()
                .flatMap(menuId -> authorityIdsByMenu.getOrDefault(menuId, List.of()).stream())
                .map(authoritiesById::get)
                .filter(Objects::nonNull)
                .map(AuthorityEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());
        return new RoleAuthorizationRpcVo(
            role.getId(),
            true,
            role.getCode(),
            role.getStatus(),
            authorityCodes,
            permissionsByRole.getOrDefault(roleId, List.of()));
    }
}
