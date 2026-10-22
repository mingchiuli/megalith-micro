package wiki.chiu.micro.blog.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringStore;

/**
 * One-release migration from the former content columns to immutable revisions.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacyBlogDataMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyBlogDataMigration.class);
    private static final String LOCK_NAME = "megalith:blog:version-migration";

    private final DataSource dataSource;
    private final BlogAuthoringStore authoring;

    public LegacyBlogDataMigration(DataSource dataSource, BlogAuthoringStore authoring) {
        this.dataSource = dataSource;
        this.authoring = authoring;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            boolean locked = acquireLock(connection);
            if (!locked) {
                throw new IllegalStateException("blog migration lock is held by another replica");
            }
            try {
                if (legacyColumnsPresent(connection)) {
                    migrate(connection);
                    backfillSearchProjections();
                }
                if (legacyArtifactsPresent(connection)) {
                    dropLegacyColumns(connection);
                    log.info("Legacy blog content migration completed");
                }
            } finally {
                releaseLock(connection);
            }
        } catch (SQLException failure) {
            throw new IllegalStateException("legacy blog content migration failed", failure);
        }
    }

    private void backfillSearchProjections() throws SQLException {
        List<Long> ids;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT id FROM m_blog ORDER BY id")) {
            var values = new ArrayList<Long>();
            while (result.next()) {
                values.add(result.getLong(1));
            }
            ids = List.copyOf(values);
        }
        ids.forEach(authoring::rebuildProjections);
    }

    private boolean legacyColumnsPresent(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'm_blog'
                  AND COLUMN_NAME IN ('content', 'status', 'event_revision')
                """)) {
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) == 3;
            }
        }
    }

    private boolean legacyArtifactsPresent(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'm_blog'
                  AND COLUMN_NAME IN ('title', 'description', 'content', 'updated',
                                      'status', 'link', 'event_revision')
                """)) {
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0
                    || tableExists(connection, "m_blog_sensitive_content");
            }
        }
    }

    private void migrate(Connection connection) throws SQLException {
        List<LegacyBlog> blogs;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                 """
                 SELECT id, user_id, title, description, content, created, updated,
                        status, link, COALESCE(read_count, 0),
                        COALESCE(event_revision, 1)
                 FROM m_blog
                 ORDER BY id
                 """)) {
            var values = new ArrayList<LegacyBlog>();
            while (result.next()) {
                values.add(
                    new LegacyBlog(
                        result.getLong(1),
                        result.getLong(2),
                        result.getString(3),
                        result.getString(4),
                        result.getString(5),
                        result.getObject(6, LocalDateTime.class),
                        result.getObject(7, LocalDateTime.class),
                        result.getInt(8),
                        result.getString(9),
                        result.getLong(10),
                        result.getLong(11)));
            }
            blogs = List.copyOf(values);
        }

        for (LegacyBlog blog : blogs) {
            migrateBlog(connection, blog);
        }
    }

    private void migrateBlog(Connection connection, LegacyBlog blog) throws SQLException {
        Long revisionId = existingRevisionId(connection, blog.id());
        if (revisionId == null) {
            boolean draftOnly = blog.status() == BlogStatusEnum.DRAFT.getCode();
            revisionId =
                insertRevision(
                    connection,
                    blog,
                    draftOnly ? BlogRevisionOrigin.DRAFT : BlogRevisionOrigin.PUBLICATION,
                    draftOnly ? null : blog.updated());
        } else {
            try (var statement =
                connection.prepareStatement(
                    "DELETE FROM m_blog_revision_sensitive_content WHERE revision_id = ?")) {
                statement.setLong(1, revisionId);
                statement.executeUpdate();
            }
        }
        copyLegacySensitive(connection, blog.id(), revisionId);

        boolean draftOnly = blog.status() == BlogStatusEnum.DRAFT.getCode();
        if (draftOnly) {
            insertDraft(connection, blog);
            updateIdentity(
                connection,
                blog,
                null,
                null,
                null,
                BlogAccessMode.PUBLIC.getCode());
        } else {
            int accessMode =
                blog.status() == BlogStatusEnum.HIDE.getCode()
                    ? BlogAccessMode.RESTRICTED.getCode()
                    : BlogAccessMode.PUBLIC.getCode();
            updateIdentity(
                connection, blog, revisionId, blog.updated(), null, accessMode);
        }
    }

    private Long existingRevisionId(Connection connection, long blogId) throws SQLException {
        try (var statement =
            connection.prepareStatement(
                "SELECT id FROM m_blog_revision WHERE blog_id = ? ORDER BY version_no LIMIT 1")) {
            statement.setLong(1, blogId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : null;
            }
        }
    }

    private Long insertRevision(
        Connection connection, LegacyBlog blog, BlogRevisionOrigin origin, LocalDateTime publishedAt)
        throws SQLException {
        try (var statement =
            connection.prepareStatement(
                """
                INSERT INTO m_blog_revision
                  (blog_id, version_no, origin, base_revision_id, title, description,
                   content, link, render_mode, first_published_at, created_by, created_at)
                VALUES (?, 1, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, blog.id());
            statement.setString(2, origin.name());
            statement.setString(3, blog.title());
            statement.setString(4, blog.description());
            statement.setString(5, blog.content());
            statement.setString(6, blog.link());
            statement.setString(
                7,
                blog.status() == BlogStatusEnum.SENSITIVE_FILTER.getCode()
                    ? BlogRenderMode.SENSITIVE_FILTER.name()
                    : BlogRenderMode.NORMAL.name());
            statement.setObject(8, publishedAt);
            statement.setLong(9, blog.userId());
            statement.setObject(10, blog.created());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private void insertDraft(Connection connection, LegacyBlog blog) throws SQLException {
        boolean exists = draftExists(connection, blog.id());
        if (exists) {
            try (var statement =
                connection.prepareStatement(
                    "DELETE FROM m_blog_draft_sensitive_content WHERE blog_id = ?")) {
                statement.setLong(1, blog.id());
                statement.executeUpdate();
            }
        } else {
            try (var statement =
                connection.prepareStatement(
                    """
                    INSERT INTO m_blog_draft
                      (blog_id, draft_revision, base_published_revision_id, title, description,
                       content, link, render_mode, updated_by, updated_at)
                    VALUES (?, 1, NULL, ?, ?, ?, ?, ?, ?, ?)
                    """)) {
                statement.setLong(1, blog.id());
                statement.setString(2, blog.title());
                statement.setString(3, blog.description());
                statement.setString(4, blog.content());
                statement.setString(5, blog.link());
                statement.setString(
                    6,
                    blog.status() == BlogStatusEnum.SENSITIVE_FILTER.getCode()
                        ? BlogRenderMode.SENSITIVE_FILTER.name()
                        : BlogRenderMode.NORMAL.name());
                statement.setLong(7, blog.userId());
                statement.setObject(8, blog.updated());
                statement.executeUpdate();
            }
        }
        copySensitive(
            connection,
            "m_blog_draft_sensitive_content",
            "blog_id",
            blog.id(),
            legacySensitive(connection, blog.id()));
    }

    private boolean draftExists(Connection connection, long blogId) throws SQLException {
        try (var statement =
            connection.prepareStatement("SELECT COUNT(*) FROM m_blog_draft WHERE blog_id = ?")) {
            statement.setLong(1, blogId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private void updateIdentity(
        Connection connection,
        LegacyBlog blog,
        Long publishedRevisionId,
        LocalDateTime publishedAt,
        LocalDateTime offlineAt,
        int accessMode)
        throws SQLException {
        try (var statement =
            connection.prepareStatement(
                """
                UPDATE m_blog
                SET published_revision_id = ?,
                    published_at = ?,
                    offline_at = ?,
                    access_mode = ?,
                    publication_revision = ?,
                    admin_revision = ?,
                    read_count = COALESCE(read_count, 0)
                WHERE id = ?
                """)) {
            statement.setObject(1, publishedRevisionId);
            statement.setObject(2, publishedAt);
            statement.setObject(3, offlineAt);
            statement.setInt(4, accessMode);
            statement.setLong(
                5, publishedRevisionId == null ? 0L : blog.adminRevision());
            statement.setLong(6, blog.adminRevision());
            statement.setLong(7, blog.id());
            statement.executeUpdate();
        }
    }

    private void copyLegacySensitive(Connection connection, long blogId, long revisionId)
        throws SQLException {
        if (!tableExists(connection, "m_blog_sensitive_content")) {
            return;
        }
        copySensitive(
            connection,
            "m_blog_revision_sensitive_content",
            "revision_id",
            revisionId,
            legacySensitive(connection, blogId));
    }

    private List<SensitiveRow> legacySensitive(Connection connection, long blogId)
        throws SQLException {
        if (!tableExists(connection, "m_blog_sensitive_content")) {
            return List.of();
        }
        var values = new ArrayList<SensitiveRow>();
        try (var statement =
            connection.prepareStatement(
                """
                SELECT start_index, end_index, type
                FROM m_blog_sensitive_content
                WHERE blog_id = ?
                """)) {
            statement.setLong(1, blogId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    values.add(
                        new SensitiveRow(
                            result.getInt(1), result.getInt(2), result.getInt(3)));
                }
            }
        }
        return values;
    }

    private void copySensitive(
        Connection connection,
        String table,
        String ownerColumn,
        long ownerId,
        List<SensitiveRow> rows)
        throws SQLException {
        if (rows.isEmpty()) {
            return;
        }
        try (var statement =
            connection.prepareStatement(
                "INSERT INTO "
                    + table
                    + " ("
                    + ownerColumn
                    + ", start_index, end_index, type) VALUES (?, ?, ?, ?)")) {
            for (SensitiveRow row : rows) {
                statement.setLong(1, ownerId);
                statement.setInt(2, row.start());
                statement.setInt(3, row.end());
                statement.setInt(4, row.type());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void dropLegacyColumns(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String column :
                List.of(
                    "title",
                    "description",
                    "content",
                    "updated",
                    "status",
                    "link",
                    "event_revision")) {
                statement.execute("ALTER TABLE m_blog DROP COLUMN IF EXISTS " + column);
            }
            statement.execute("DROP TABLE IF EXISTS m_blog_sensitive_content");
        }
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        try (var statement =
            connection.prepareStatement(
                """
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                """)) {
            statement.setString(1, table);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private boolean acquireLock(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT GET_LOCK(?, 30)")) {
            statement.setString(1, LOCK_NAME);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) == 1;
            }
        }
    }

    private void releaseLock(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            statement.setString(1, LOCK_NAME);
            statement.executeQuery().close();
        }
    }

    private record LegacyBlog(
        long id,
        long userId,
        String title,
        String description,
        String content,
        LocalDateTime created,
        LocalDateTime updated,
        int status,
        String link,
        long readCount,
        long adminRevision) {
    }

    private record SensitiveRow(int start, int end, int type) {
    }
}
