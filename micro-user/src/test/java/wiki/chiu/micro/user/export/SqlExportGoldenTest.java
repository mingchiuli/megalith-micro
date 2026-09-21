package wiki.chiu.micro.user.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.export.SQLUtils;
import wiki.chiu.micro.user.application.model.SqlTables;
import wiki.chiu.micro.user.domain.AuthorityEntity;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;
import wiki.chiu.micro.user.domain.MenuEntity;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.RoleMenuEntity;
import wiki.chiu.micro.user.domain.UserEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;

/**
 * Pins the export statements to the output produced before the reflective renderer was replaced.
 */
class SqlExportGoldenTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 9, 19, 10, 11, 12);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2026, 9, 19, 11, 12, 13);

    @Test
    void userTableMatchesLegacyOutput() {
        var entity =
            new UserEntity(
                1L,
                "alice",
                "nick'name",
                "avatar",
                "a@b.com",
                "13800000000",
                "pwd",
                1,
                UPDATED,
                CREATED,
                UPDATED,
                UPDATED);

        assertEquals(
            "INSERT INTO m_user (id, username, nickname, avatar, email, phone, password, status,"
                + " password_locked_until, created, updated, last_login) VALUES (1, 'alice',"
                + " 'nick''name', 'avatar', 'a@b.com', '13800000000', 'pwd', 1, '2026-09-19"
                + " 11:12:13', '2026-09-19 10:11:12', '2026-09-19 11:12:13', '2026-09-19"
                + " 11:12:13');",
            SQLUtils.insertSql(List.of(entity), SqlTables.USER));
    }

    @Test
    void multipleRowsAreJoinedAndNullsAreRendered() {
        var first = new UserEntity(1L, "alice", "nick", "avatar", "a@b.com", "138", "pwd", 1, null,
            CREATED, UPDATED, UPDATED);

        assertEquals(
            "INSERT INTO m_user (id, username, nickname, avatar, email, phone, password, status,"
                + " password_locked_until, created, updated, last_login) VALUES (1, 'alice',"
                + " 'nick', 'avatar', 'a@b.com', '138', 'pwd', 1, NULL, '2026-09-19 10:11:12',"
                + " '2026-09-19 11:12:13', '2026-09-19 11:12:13'),\n"
                + "(NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);",
            SQLUtils.insertSql(List.of(first, new UserEntity()), SqlTables.USER));
    }

    @Test
    void emptyInputProducesNoStatement() {
        assertEquals("", SQLUtils.insertSql(List.of(), SqlTables.USER));
    }

    @Test
    void roleTablesMatchLegacyOutput() {
        assertEquals(
            "INSERT INTO m_role (id, name, code, remark, created, updated, status) VALUES (2,"
                + " 'admin', 'ADMIN', 'remark', '2026-09-19 10:11:12', '2026-09-19 11:12:13', 1);",
            SQLUtils.insertSql(
                List.of(new RoleEntity(2L, "admin", "ADMIN", "remark", CREATED, UPDATED, 1)),
                SqlTables.ROLE));
        assertEquals(
            "INSERT INTO m_user_role (id, user_id, role_id, created, updated) VALUES (3, 1, 2,"
                + " '2026-09-19 10:11:12', '2026-09-19 11:12:13');",
            SQLUtils.insertSql(
                List.of(new UserRoleEntity(3L, 1L, 2L, CREATED, UPDATED)), SqlTables.USER_ROLE));
        assertEquals(
            "INSERT INTO m_role_data_permission (id, role_id, permission_code, created, updated)"
                + " VALUES (NULL, 2, 'BLOG_EXPORT_ALL', NULL, NULL);",
            SQLUtils.insertSql(
                List.of(new RoleDataPermissionEntity(2L, DataPermissionEnum.BLOG_EXPORT_ALL)),
                SqlTables.ROLE_DATA_PERMISSION));
    }

    @Test
    void menuTablesMatchLegacyOutput() {
        assertEquals(
            "INSERT INTO m_menu (id, parent_id, title, name, url, component, type, icon, order_num,"
                + " status, created, updated) VALUES (4, 0, 'title', 'menu', '/url', 'comp', 1,"
                + " 'icon', 9, 1, '2026-09-19 10:11:12', '2026-09-19 11:12:13');",
            SQLUtils.insertSql(
                List.of(
                    new MenuEntity(
                        4L, 0L, "title", "menu", "/url", "comp", 1, "icon", 9, 1, CREATED, UPDATED)),
                SqlTables.MENU));
        assertEquals(
            "INSERT INTO m_role_menu (id, role_id, menu_id, created, updated) VALUES (5, 2, 4,"
                + " '2026-09-19 10:11:12', '2026-09-19 11:12:13');",
            SQLUtils.insertSql(
                List.of(new RoleMenuEntity(5L, 2L, 4L, CREATED, UPDATED)), SqlTables.ROLE_MENU));
    }

    @Test
    void authorityTablesMatchLegacyOutput() {
        assertEquals(
            "INSERT INTO m_authority (id, code, remark, prototype, method_type, route_pattern,"
                + " service_host, service_port, created, updated, type, status) VALUES (6, 'code',"
                + " 'remark', 'proto', 'GET', '/inner/x', 'micro-user', 8086, '2026-09-19"
                + " 10:11:12', '2026-09-19 11:12:13', 2, 1);",
            SQLUtils.insertSql(
                List.of(
                    new AuthorityEntity(
                        6L,
                        "code",
                        "remark",
                        "proto",
                        "GET",
                        "/inner/x",
                        "micro-user",
                        8086,
                        CREATED,
                        UPDATED,
                        2,
                        1)),
                SqlTables.AUTHORITY));
        assertEquals(
            "INSERT INTO m_menu_authority (id, menu_id, authority_id, created, updated) VALUES (7,"
                + " 4, 6, '2026-09-19 10:11:12', '2026-09-19 11:12:13');",
            SQLUtils.insertSql(
                List.of(new MenuAuthorityEntity(7L, 4L, 6L, CREATED, UPDATED)),
                SqlTables.MENU_AUTHORITY));
    }
}
