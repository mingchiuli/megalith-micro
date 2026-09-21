package wiki.chiu.micro.user.application.model;

import static wiki.chiu.micro.common.constant.Const.AUTHORITY_TABLE;
import static wiki.chiu.micro.common.constant.Const.MENU_AUTHORITY_TABLE;
import static wiki.chiu.micro.common.constant.Const.MENU_TABLE;
import static wiki.chiu.micro.common.constant.Const.ROLE_DATA_PERMISSION_TABLE;
import static wiki.chiu.micro.common.constant.Const.ROLE_MENU_TABLE;
import static wiki.chiu.micro.common.constant.Const.ROLE_TABLE;
import static wiki.chiu.micro.common.constant.Const.USER_ROLE_TABLE;
import static wiki.chiu.micro.common.constant.Const.USER_TABLE;

import wiki.chiu.micro.common.export.SqlColumn;
import wiki.chiu.micro.common.export.SqlTable;
import wiki.chiu.micro.user.domain.AuthorityEntity;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;
import wiki.chiu.micro.user.domain.MenuEntity;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.RoleMenuEntity;
import wiki.chiu.micro.user.domain.UserEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;

/**
 * Explicit SQL export column definitions for the user service tables.
 */
public final class SqlTables {

    public static final SqlTable<UserEntity> USER =
        SqlTable.of(
            USER_TABLE,
            SqlColumn.of("id", UserEntity::getId),
            SqlColumn.of("username", UserEntity::getUsername),
            SqlColumn.of("nickname", UserEntity::getNickname),
            SqlColumn.of("avatar", UserEntity::getAvatar),
            SqlColumn.of("email", UserEntity::getEmail),
            SqlColumn.of("phone", UserEntity::getPhone),
            SqlColumn.of("password", UserEntity::getPassword),
            SqlColumn.of("status", UserEntity::getStatus),
            SqlColumn.of("password_locked_until", UserEntity::getPasswordLockedUntil),
            SqlColumn.of("created", UserEntity::getCreated),
            SqlColumn.of("updated", UserEntity::getUpdated),
            SqlColumn.of("last_login", UserEntity::getLastLogin));

    public static final SqlTable<RoleEntity> ROLE =
        SqlTable.of(
            ROLE_TABLE,
            SqlColumn.of("id", RoleEntity::getId),
            SqlColumn.of("name", RoleEntity::getName),
            SqlColumn.of("code", RoleEntity::getCode),
            SqlColumn.of("remark", RoleEntity::getRemark),
            SqlColumn.of("created", RoleEntity::getCreated),
            SqlColumn.of("updated", RoleEntity::getUpdated),
            SqlColumn.of("status", RoleEntity::getStatus));

    public static final SqlTable<UserRoleEntity> USER_ROLE =
        SqlTable.of(
            USER_ROLE_TABLE,
            SqlColumn.of("id", UserRoleEntity::getId),
            SqlColumn.of("user_id", UserRoleEntity::getUserId),
            SqlColumn.of("role_id", UserRoleEntity::getRoleId),
            SqlColumn.of("created", UserRoleEntity::getCreated),
            SqlColumn.of("updated", UserRoleEntity::getUpdated));

    public static final SqlTable<RoleDataPermissionEntity> ROLE_DATA_PERMISSION =
        SqlTable.of(
            ROLE_DATA_PERMISSION_TABLE,
            SqlColumn.of("id", RoleDataPermissionEntity::getId),
            SqlColumn.of("role_id", RoleDataPermissionEntity::getRoleId),
            SqlColumn.of("permission_code", RoleDataPermissionEntity::getPermissionCode),
            SqlColumn.of("created", RoleDataPermissionEntity::getCreated),
            SqlColumn.of("updated", RoleDataPermissionEntity::getUpdated));

    public static final SqlTable<MenuEntity> MENU =
        SqlTable.of(
            MENU_TABLE,
            SqlColumn.of("id", MenuEntity::getId),
            SqlColumn.of("parent_id", MenuEntity::getParentId),
            SqlColumn.of("title", MenuEntity::getTitle),
            SqlColumn.of("name", MenuEntity::getName),
            SqlColumn.of("url", MenuEntity::getUrl),
            SqlColumn.of("component", MenuEntity::getComponent),
            SqlColumn.of("type", MenuEntity::getType),
            SqlColumn.of("icon", MenuEntity::getIcon),
            SqlColumn.of("order_num", MenuEntity::getOrderNum),
            SqlColumn.of("status", MenuEntity::getStatus),
            SqlColumn.of("created", MenuEntity::getCreated),
            SqlColumn.of("updated", MenuEntity::getUpdated));

    public static final SqlTable<RoleMenuEntity> ROLE_MENU =
        SqlTable.of(
            ROLE_MENU_TABLE,
            SqlColumn.of("id", RoleMenuEntity::getId),
            SqlColumn.of("role_id", RoleMenuEntity::getRoleId),
            SqlColumn.of("menu_id", RoleMenuEntity::getMenuId),
            SqlColumn.of("created", RoleMenuEntity::getCreated),
            SqlColumn.of("updated", RoleMenuEntity::getUpdated));

    public static final SqlTable<AuthorityEntity> AUTHORITY =
        SqlTable.of(
            AUTHORITY_TABLE,
            SqlColumn.of("id", AuthorityEntity::getId),
            SqlColumn.of("code", AuthorityEntity::getCode),
            SqlColumn.of("remark", AuthorityEntity::getRemark),
            SqlColumn.of("prototype", AuthorityEntity::getPrototype),
            SqlColumn.of("method_type", AuthorityEntity::getMethodType),
            SqlColumn.of("route_pattern", AuthorityEntity::getRoutePattern),
            SqlColumn.of("service_host", AuthorityEntity::getServiceHost),
            SqlColumn.of("service_port", AuthorityEntity::getServicePort),
            SqlColumn.of("created", AuthorityEntity::getCreated),
            SqlColumn.of("updated", AuthorityEntity::getUpdated),
            SqlColumn.of("type", AuthorityEntity::getType),
            SqlColumn.of("status", AuthorityEntity::getStatus));

    public static final SqlTable<MenuAuthorityEntity> MENU_AUTHORITY =
        SqlTable.of(
            MENU_AUTHORITY_TABLE,
            SqlColumn.of("id", MenuAuthorityEntity::getId),
            SqlColumn.of("menu_id", MenuAuthorityEntity::getMenuId),
            SqlColumn.of("authority_id", MenuAuthorityEntity::getAuthorityId),
            SqlColumn.of("created", MenuAuthorityEntity::getCreated),
            SqlColumn.of("updated", MenuAuthorityEntity::getUpdated));

    private SqlTables() {
    }
}
