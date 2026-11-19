package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.user.adapter.out.persistence.convertor.UserPersistenceConvertor;
import wiki.chiu.micro.user.adapter.out.persistence.entity.AuthorityEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleDataPermissionEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleMenuEntity;
import wiki.chiu.micro.user.adapter.out.persistence.repository.AuthorityRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleMenuRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRoleRepository;
import wiki.chiu.micro.user.application.model.RoleAuthorization;
import wiki.chiu.micro.user.application.model.UserAccess;
import wiki.chiu.micro.user.application.port.out.AuthorizationReader;

@Component
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class JpaAuthorizationReader implements AuthorizationReader {

    private final UserRepository users;
    private final UserRoleRepository userRoles;
    private final RoleRepository roles;
    private final RoleMenuRepository roleMenus;
    private final MenuAuthorityRepository menuAuthorities;
    private final AuthorityRepository authorities;
    private final RoleDataPermissionRepository dataPermissions;

    public JpaAuthorizationReader(
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

    @Override
    public UserAccess findUserAccess(Long userId) {
        return users
            .findById(userId)
            .map(user -> UserPersistenceConvertor.toUserAccess(user, userRoles.findByUserId(userId)))
            .orElseGet(() -> UserAccess.missing(userId));
    }

    @Override
    public List<RoleAuthorization> findRoleAuthorizations(List<Long> roleIds) {
        List<Long> distinctRoleIds = roleIds.stream().distinct().toList();
        if (distinctRoleIds.isEmpty()) {
            return List.of();
        }

        Map<Long, RoleEntity> rolesById =
            roles.findAllById(distinctRoleIds).stream()
                .collect(Collectors.toMap(RoleEntity::getId, Function.identity()));
        return findRoleAuthorizationsInternal(distinctRoleIds, rolesById);
    }

    @Override
    public List<RoleAuthorization> findAllRoleAuthorizations() {
        List<RoleEntity> allRoles = roles.findAll();
        List<Long> distinctRoleIds = allRoles.stream().map(RoleEntity::getId).distinct().toList();
        if (distinctRoleIds.isEmpty()) {
            return List.of();
        }

        Map<Long, RoleEntity> rolesById =
            allRoles.stream().collect(Collectors.toMap(RoleEntity::getId, Function.identity()));
        return findRoleAuthorizationsInternal(distinctRoleIds, rolesById);
    }

    private List<RoleAuthorization> findRoleAuthorizationsInternal(
        List<Long> distinctRoleIds, Map<Long, RoleEntity> rolesById) {
        List<Long> existingRoleIds = distinctRoleIds.stream().filter(rolesById::containsKey).toList();
        if (existingRoleIds.isEmpty()) {
            return distinctRoleIds.stream().map(RoleAuthorization::missing).toList();
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
                    UserPersistenceConvertor.toAuthorization(
                        roleId,
                        rolesById,
                        menuIdsByRole,
                        authorityIdsByMenu,
                        authoritiesById,
                        permissionsByRole))
            .toList();
    }
}
