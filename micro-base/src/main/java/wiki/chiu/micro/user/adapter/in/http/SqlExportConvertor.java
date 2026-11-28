package wiki.chiu.micro.user.adapter.in.http;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wiki.chiu.micro.common.export.SQLUtils;
import wiki.chiu.micro.user.application.model.AuthorityExport;
import wiki.chiu.micro.user.application.model.MenuExport;
import wiki.chiu.micro.user.application.model.RoleExport;
import wiki.chiu.micro.user.application.model.UserExport;
import wiki.chiu.micro.user.domain.Menu;

/**
 * Renders the export use-case results as the SQL scripts the download endpoints deliver. The script
 * format is delivery vocabulary, so it lives in the adapter that offers the download.
 */
public final class SqlExportConvertor {

    private SqlExportConvertor() {
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
                SQLUtils.insertSql(parentFirst(export.menus()), SqlTables.MENU),
                SQLUtils.insertSql(export.roleMenus(), SqlTables.ROLE_MENU))
            .getBytes(StandardCharsets.UTF_8);
    }

    private static List<Menu> parentFirst(List<Menu> menus) {
        Map<Long, Menu> byId = new HashMap<>();
        menus.forEach(menu -> byId.put(menu.id(), menu));
        List<Menu> ordered = new ArrayList<>(menus.size());
        Set<Long> written = new HashSet<>();
        for (Menu menu : menus) {
            List<Menu> ancestors = new ArrayList<>();
            Set<Long> pending = new HashSet<>();
            for (Menu next = menu; next != null && !written.contains(next.id()); next = byId.get(next.parentId())) {
                if (!pending.add(next.id())) {
                    throw new IllegalStateException("Cyclic menu hierarchy at " + next.id());
                }
                ancestors.add(next);
            }
            for (Menu ancestor : ancestors.reversed()) {
                ordered.add(ancestor);
                written.add(ancestor.id());
            }
        }
        return ordered;
    }

    public static byte[] toAuthoritySql(AuthorityExport export) {
        return SQLUtils.compose(
                SQLUtils.insertSql(export.authorities(), SqlTables.AUTHORITY),
                SQLUtils.insertSql(export.menuAuthorities(), SqlTables.MENU_AUTHORITY))
            .getBytes(StandardCharsets.UTF_8);
    }
}
