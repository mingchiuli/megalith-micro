package wiki.chiu.micro.user.adapter.in.http;

import java.nio.charset.StandardCharsets;

import wiki.chiu.micro.common.export.SQLUtils;
import wiki.chiu.micro.user.application.model.AuthorityExport;
import wiki.chiu.micro.user.application.model.MenuExport;
import wiki.chiu.micro.user.application.model.RoleExport;
import wiki.chiu.micro.user.application.model.UserExport;

/**
 * Renders the export use-case results as the SQL scripts the download endpoints deliver. The script
 * format is delivery vocabulary, so it lives in the adapter that offers the download.
 */
public final class SqlExportMapper {

    private SqlExportMapper() {
    }

    public static byte[] toUserSql(UserExport export) {
        return SQLUtils.insertSql(export.users(), SqlTables.USER).getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] toRoleSql(RoleExport export) {
        return SQLUtils.compose(
                SQLUtils.insertSql(export.roles(), SqlTables.ROLE),
                SQLUtils.insertSql(export.userRoles(), SqlTables.USER_ROLE),
                SQLUtils.insertSql(export.dataPermissions(), SqlTables.ROLE_DATA_PERMISSION))
            .getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] toMenuSql(MenuExport export) {
        return SQLUtils.compose(
                SQLUtils.insertSql(export.menus(), SqlTables.MENU),
                SQLUtils.insertSql(export.roleMenus(), SqlTables.ROLE_MENU))
            .getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] toAuthoritySql(AuthorityExport export) {
        return SQLUtils.compose(
                SQLUtils.insertSql(export.authorities(), SqlTables.AUTHORITY),
                SQLUtils.insertSql(export.menuAuthorities(), SqlTables.MENU_AUTHORITY))
            .getBytes(StandardCharsets.UTF_8);
    }
}
