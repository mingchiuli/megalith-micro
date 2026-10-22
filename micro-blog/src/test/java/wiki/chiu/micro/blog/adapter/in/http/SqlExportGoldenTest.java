package wiki.chiu.micro.blog.adapter.in.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.model.BlogExportPage;
import wiki.chiu.micro.blog.application.model.BlogExportRow;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.export.SQLUtils;

class SqlExportGoldenTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 9, 19, 10, 11, 12);

    @Test
    void exportsNormalizedIdentityAndRevisionTables() {
        BlogIdentity identity =
            new BlogIdentity(
                1L, 42L, CREATED, 7L, 2L, CREATED, null, BlogAccessMode.PUBLIC, 1L, 3L);
        BlogVersion version =
            new BlogVersion(
                2L,
                1L,
                1L,
                BlogRevisionOrigin.PUBLICATION,
                null,
                "title",
                "desc",
                "line1\nline2",
                "",
                BlogRenderMode.NORMAL,
                CREATED,
                42L,
                CREATED,
                List.of());
        BlogExportPage page =
            new BlogExportPage(List.of(new BlogExportRow(identity, List.of(version))));

        assertEquals(
            "INSERT INTO m_blog (id, user_id, created, read_count, published_revision_id,"
                + " published_at, offline_at, access_mode, publication_revision, admin_revision)"
                + " VALUES (1, 42, '2026-09-19 10:11:12', 7, 2, '2026-09-19 10:11:12',"
                + " NULL, 0, 1, 3);",
            SQLUtils.insertSql(page.blogs(), SqlTables.BLOG));
        assertEquals(
            "INSERT INTO m_blog_revision (id, blog_id, version_no, origin, base_revision_id,"
                + " title, description, content, link, render_mode, first_published_at,"
                + " created_by, created_at) VALUES (2, 1, 1, 'PUBLICATION', NULL, 'title',"
                + " 'desc', 'line1\\nline2', '', 'NORMAL', '2026-09-19 10:11:12', 42,"
                + " '2026-09-19 10:11:12');",
            SQLUtils.insertSql(page.revisions(), SqlTables.BLOG_REVISION));
    }
}
