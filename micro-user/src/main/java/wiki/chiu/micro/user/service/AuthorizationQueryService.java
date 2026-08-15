package wiki.chiu.micro.user.service;

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
import wiki.chiu.micro.user.entity.AuthorityEntity;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.entity.RoleDataPermissionEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.entity.UserRoleEntity;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;

@Service
public class AuthorizationQueryService {

  private final UserRepository users;
  private final UserRoleRepository userRoles;
  private final RoleRepository roles;
  private final RoleMenuRepository roleMenus;
  private final MenuAuthorityRepository menuAuthorities;
  private final AuthorityRepository authorities;
  private final RoleDataPermissionRepository dataPermissions;

  public AuthorizationQueryService(
      UserRepository users,
      UserRoleRepository userRoles,
      RoleRepository roles,
      RoleMenuRepository roleMenus,
      MenuAuthorityRepository menuAuthorities,
      AuthorityRepository authorities,
      RoleDataPermissionRepository dataPermissions) {
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

  public RoleAuthorizationRpcVo findRoleAuthorization(Long roleId) {
    return findRoleAuthorizationsInternal(List.of(roleId)).getFirst();
  }

  public List<RoleAuthorizationRpcVo> findRoleAuthorizations(List<Long> roleIds) {
    return findRoleAuthorizationsInternal(roleIds);
  }

  private List<RoleAuthorizationRpcVo> findRoleAuthorizationsInternal(List<Long> roleIds) {
    List<Long> distinctRoleIds = roleIds.stream().distinct().toList();
    if (distinctRoleIds.isEmpty()) {
      return List.of();
    }

    Map<Long, RoleEntity> rolesById =
        roles.findAllById(distinctRoleIds).stream()
            .collect(Collectors.toMap(RoleEntity::getId, Function.identity()));
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
