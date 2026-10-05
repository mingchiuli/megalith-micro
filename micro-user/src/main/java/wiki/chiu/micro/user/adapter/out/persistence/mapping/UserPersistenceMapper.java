package wiki.chiu.micro.user.adapter.out.persistence.mapping;

import java.util.List;

import wiki.chiu.micro.user.adapter.out.persistence.entity.AuthorityEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.MenuEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleDataPermissionEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleMenuEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.UserEntity;
import wiki.chiu.micro.user.adapter.out.persistence.entity.UserRoleEntity;
import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.Menu;
import wiki.chiu.micro.user.domain.MenuAuthority;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.RoleDataPermission;
import wiki.chiu.micro.user.domain.RoleMenu;
import wiki.chiu.micro.user.domain.User;
import wiki.chiu.micro.user.domain.UserRole;

/**
 * Translates between the persisted entities and the framework-free domain model, so only this
 * adapter knows the JPA mapping.
 */
public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static User toDomain(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getUsername(),
            entity.getNickname(),
            entity.getAvatar(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getPassword(),
            entity.getStatus(),
            entity.getPasswordLockedUntil(),
            entity.getCreated(),
            entity.getUpdated(),
            entity.getLastLogin());
    }

    public static List<User> toUsers(List<UserEntity> entities) {
        return entities.stream().map(UserPersistenceMapper::toDomain).toList();
    }

    public static UserEntity toEntity(User user) {
        return new UserEntity(
            user.id(),
            user.username(),
            user.nickname(),
            user.avatar(),
            user.email(),
            user.phone(),
            user.password(),
            user.status(),
            user.passwordLockedUntil(),
            user.created(),
            user.updated(),
            user.lastLogin());
    }

    public static Role toDomain(RoleEntity entity) {
        return new Role(
            entity.getId(),
            entity.getName(),
            entity.getCode(),
            entity.getRemark(),
            entity.getCreated(),
            entity.getUpdated(),
            entity.getStatus());
    }

    public static List<Role> toRoles(List<RoleEntity> entities) {
        return entities.stream().map(UserPersistenceMapper::toDomain).toList();
    }

    public static RoleEntity toEntity(Role role) {
        return new RoleEntity(
            role.id(), role.name(), role.code(), role.remark(), role.created(), role.updated(),
            role.status());
    }

    public static Menu toDomain(MenuEntity entity) {
        return new Menu(
            entity.getId(),
            entity.getParentId(),
            entity.getTitle(),
            entity.getName(),
            entity.getUrl(),
            entity.getComponent(),
            entity.getType(),
            entity.getIcon(),
            entity.getOrderNum(),
            entity.getStatus(),
            entity.getCreated(),
            entity.getUpdated());
    }

    public static List<Menu> toMenus(List<MenuEntity> entities) {
        return entities.stream().map(UserPersistenceMapper::toDomain).toList();
    }

    public static MenuEntity toEntity(Menu menu) {
        return new MenuEntity(
            menu.id(),
            menu.parentId(),
            menu.title(),
            menu.name(),
            menu.url(),
            menu.component(),
            menu.type(),
            menu.icon(),
            menu.orderNum(),
            menu.status(),
            menu.created(),
            menu.updated());
    }

    public static Authority toDomain(AuthorityEntity entity) {
        return new Authority(
            entity.getId(),
            entity.getCode(),
            entity.getRemark(),
            entity.getPrototype(),
            entity.getMethodType(),
            entity.getRoutePattern(),
            entity.getServiceHost(),
            entity.getServicePort(),
            entity.getCreated(),
            entity.getUpdated(),
            entity.getType(),
            entity.getStatus());
    }

    public static List<Authority> toAuthorities(List<AuthorityEntity> entities) {
        return entities.stream().map(UserPersistenceMapper::toDomain).toList();
    }

    public static AuthorityEntity toEntity(Authority authority) {
        return new AuthorityEntity(
            authority.id(),
            authority.code(),
            authority.remark(),
            authority.prototype(),
            authority.methodType(),
            authority.routePattern(),
            authority.serviceHost(),
            authority.servicePort(),
            authority.created(),
            authority.updated(),
            authority.type(),
            authority.status());
    }

    public static UserRole toDomain(UserRoleEntity entity) {
        return new UserRole(
            entity.getId(), entity.getUserId(), entity.getRoleId(), entity.getCreated(),
            entity.getUpdated());
    }

    public static List<UserRole> toUserRoles(List<UserRoleEntity> entities) {
        return entities.stream().map(UserPersistenceMapper::toDomain).toList();
    }

    public static UserRoleEntity toEntity(UserRole userRole) {
        return new UserRoleEntity(
            userRole.id(), userRole.userId(), userRole.roleId(), userRole.created(),
            userRole.updated());
    }

    public static RoleMenu toDomain(RoleMenuEntity entity) {
        return new RoleMenu(
            entity.getId(), entity.getRoleId(), entity.getMenuId(), entity.getCreated(),
            entity.getUpdated());
    }

    public static List<RoleMenu> toRoleMenus(List<RoleMenuEntity> entities) {
        return entities.stream().map(UserPersistenceMapper::toDomain).toList();
    }

    public static RoleMenuEntity toEntity(RoleMenu roleMenu) {
        return new RoleMenuEntity(
            roleMenu.id(), roleMenu.roleId(), roleMenu.menuId(), roleMenu.created(),
            roleMenu.updated());
    }

    public static MenuAuthority toDomain(MenuAuthorityEntity entity) {
        return new MenuAuthority(
            entity.getId(), entity.getMenuId(), entity.getAuthorityId(), entity.getCreated(),
            entity.getUpdated());
    }

    public static List<MenuAuthority> toMenuAuthorities(List<MenuAuthorityEntity> entities) {
        return entities.stream().map(UserPersistenceMapper::toDomain).toList();
    }

    public static MenuAuthorityEntity toEntity(MenuAuthority menuAuthority) {
        return new MenuAuthorityEntity(
            menuAuthority.id(), menuAuthority.menuId(), menuAuthority.authorityId(),
            menuAuthority.created(), menuAuthority.updated());
    }

    public static RoleDataPermission toDomain(RoleDataPermissionEntity entity) {
        return new RoleDataPermission(
            entity.getId(), entity.getRoleId(), entity.permission(), entity.getCreated(),
            entity.getUpdated());
    }

    public static List<RoleDataPermission> toRoleDataPermissions(
        List<RoleDataPermissionEntity> entities) {
        return entities.stream().map(UserPersistenceMapper::toDomain).toList();
    }

    public static RoleDataPermissionEntity toEntity(RoleDataPermission permission) {
        return new RoleDataPermissionEntity(permission.roleId(), permission.permission());
    }
}
