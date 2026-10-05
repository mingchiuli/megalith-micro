package wiki.chiu.micro.user.adapter.in.http;

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
import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.MenuAuthority;
import wiki.chiu.micro.user.domain.Menu;
import wiki.chiu.micro.user.domain.RoleDataPermission;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.RoleMenu;
import wiki.chiu.micro.user.domain.User;
import wiki.chiu.micro.user.domain.UserRole;

/**
 * The physical tables and columns of the SQL scripts the download endpoints deliver, mapped from
 * the exported domain rows.
 */
public final class SqlTables {

    public static final SqlTable<User> USER =
        SqlTable.of(
            USER_TABLE,
            SqlColumn.of("id", User::id),
            SqlColumn.of("username", User::username),
            SqlColumn.of("nickname", User::nickname),
            SqlColumn.of("avatar", User::avatar),
            SqlColumn.of("email", User::email),
            SqlColumn.of("phone", User::phone),
            SqlColumn.of("password", User::password),
            SqlColumn.of("status", User::status),
            SqlColumn.of("password_locked_until", User::passwordLockedUntil),
            SqlColumn.of("created", User::created),
            SqlColumn.of("updated", User::updated),
            SqlColumn.of("last_login", User::lastLogin));

    public static final SqlTable<Role> ROLE =
        SqlTable.of(
            ROLE_TABLE,
            SqlColumn.of("id", Role::id),
            SqlColumn.of("name", Role::name),
            SqlColumn.of("code", Role::code),
            SqlColumn.of("remark", Role::remark),
            SqlColumn.of("created", Role::created),
            SqlColumn.of("updated", Role::updated),
            SqlColumn.of("status", Role::status));

    public static final SqlTable<UserRole> USER_ROLE =
        SqlTable.of(
            USER_ROLE_TABLE,
            SqlColumn.of("id", UserRole::id),
            SqlColumn.of("user_id", UserRole::userId),
            SqlColumn.of("role_id", UserRole::roleId),
            SqlColumn.of("created", UserRole::created),
            SqlColumn.of("updated", UserRole::updated));

    public static final SqlTable<RoleDataPermission> ROLE_DATA_PERMISSION =
        SqlTable.of(
            ROLE_DATA_PERMISSION_TABLE,
            SqlColumn.of("id", RoleDataPermission::id),
            SqlColumn.of("role_id", RoleDataPermission::roleId),
            SqlColumn.of("permission_code", permission -> permission.permission().name()),
            SqlColumn.of("created", RoleDataPermission::created),
            SqlColumn.of("updated", RoleDataPermission::updated));

    public static final SqlTable<Menu> MENU =
        SqlTable.of(
            MENU_TABLE,
            SqlColumn.of("id", Menu::id),
            SqlColumn.of("parent_id", Menu::parentId),
            SqlColumn.of("title", Menu::title),
            SqlColumn.of("name", Menu::name),
            SqlColumn.of("url", Menu::url),
            SqlColumn.of("component", Menu::component),
            SqlColumn.of("type", Menu::type),
            SqlColumn.of("icon", Menu::icon),
            SqlColumn.of("order_num", Menu::orderNum),
            SqlColumn.of("status", Menu::status),
            SqlColumn.of("created", Menu::created),
            SqlColumn.of("updated", Menu::updated));

    public static final SqlTable<RoleMenu> ROLE_MENU =
        SqlTable.of(
            ROLE_MENU_TABLE,
            SqlColumn.of("id", RoleMenu::id),
            SqlColumn.of("role_id", RoleMenu::roleId),
            SqlColumn.of("menu_id", RoleMenu::menuId),
            SqlColumn.of("created", RoleMenu::created),
            SqlColumn.of("updated", RoleMenu::updated));

    public static final SqlTable<Authority> AUTHORITY =
        SqlTable.of(
            AUTHORITY_TABLE,
            SqlColumn.of("id", Authority::id),
            SqlColumn.of("code", Authority::code),
            SqlColumn.of("remark", Authority::remark),
            SqlColumn.of("prototype", Authority::prototype),
            SqlColumn.of("method_type", Authority::methodType),
            SqlColumn.of("route_pattern", Authority::routePattern),
            SqlColumn.of("service_host", Authority::serviceHost),
            SqlColumn.of("service_port", Authority::servicePort),
            SqlColumn.of("created", Authority::created),
            SqlColumn.of("updated", Authority::updated),
            SqlColumn.of("type", Authority::type),
            SqlColumn.of("status", Authority::status));

    public static final SqlTable<MenuAuthority> MENU_AUTHORITY =
        SqlTable.of(
            MENU_AUTHORITY_TABLE,
            SqlColumn.of("id", MenuAuthority::id),
            SqlColumn.of("menu_id", MenuAuthority::menuId),
            SqlColumn.of("authority_id", MenuAuthority::authorityId),
            SqlColumn.of("created", MenuAuthority::created),
            SqlColumn.of("updated", MenuAuthority::updated));

    private SqlTables() {
    }
}
