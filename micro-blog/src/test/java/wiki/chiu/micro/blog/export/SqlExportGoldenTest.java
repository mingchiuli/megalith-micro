package wiki.chiu.micro.blog.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.blog.application.model.SqlTables;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.domain.BlogSensitiveContentEntity;
import wiki.chiu.micro.common.export.SQLUtils;

/**
 * Pins the export statements to the output produced before the reflective renderer was replaced.
 */
class SqlExportGoldenTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 9, 19, 10, 11, 12);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2026, 9, 19, 11, 12, 13);

    @Test
    void blogTableMatchesLegacyOutput() {
        var entity =
            new BlogEntity(
                1L, 42L, "title", "desc", "line1\nline2", CREATED, UPDATED, 1, "link", 7L, 11L);

        assertEquals(
            "INSERT INTO m_blog (id, user_id, title, description, content, created, updated,"
                + " status, link, read_count, event_revision) VALUES (1, 42, 'title', 'desc',"
                + " 'line1\\nline2', '2026-09-19 10:11:12', '2026-09-19 11:12:13', 1, 'link', 7,"
                + " 11);",
            SQLUtils.insertSql(List.of(entity), SqlTables.BLOG));
    }

    @Test
    void sensitiveContentMatchesLegacyOutput() {
        assertEquals(
            "INSERT INTO m_blog_sensitive_content (id, blog_id, start_index, end_index, type,"
                + " created, updated) VALUES (2, 1, 3, 5, 1, '2026-09-19 10:11:12', '2026-09-19"
                + " 11:12:13');",
            SQLUtils.insertSql(
                List.of(new BlogSensitiveContentEntity(2L, 1L, 3, 5, 1, CREATED, UPDATED)),
                SqlTables.BLOG_SENSITIVE));
    }
}
