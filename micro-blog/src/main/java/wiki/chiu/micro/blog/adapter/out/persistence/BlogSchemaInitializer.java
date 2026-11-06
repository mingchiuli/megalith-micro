package wiki.chiu.micro.blog.adapter.out.persistence;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/** Recognized by Boot's database-initializer ordering, before Hibernate schema updates. */
public final class BlogSchemaInitializer extends DataSourceScriptDatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(BlogSchemaInitializer.class);

    public BlogSchemaInitializer(DataSource dataSource) {
        super(dataSource, new DatabaseInitializationSettings());
    }

    @Override
    public boolean initializeDatabase() {
        try (Connection connection = getDataSource().getConnection()) {
            String catalog = connection.getCatalog();
            if (catalog == null) {
                throw new SQLException("Blog schema migration requires a selected database");
            }
            String lock = "megalith:blog-epoch:"
                + UUID.nameUUIDFromBytes(catalog.getBytes(StandardCharsets.UTF_8));
            acquireLock(connection, lock);
            try {
                if (!tableExists(connection) || epochIsRequired(connection)) {
                    return false;
                }
                ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/migration/add-blog-collaboration-epoch.sql"));
                log.info("Completed blog collaboration epoch schema migration");
                return true;
            } finally {
                releaseLock(connection, lock);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Blog collaboration epoch schema migration failed", exception);
        }
    }

    private boolean tableExists(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement("""
            SELECT 1 FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'm_blog'
            """); var result = statement.executeQuery()) {
            return result.next();
        }
    }

    private boolean epochIsRequired(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement("""
            SELECT IS_NULLABLE FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'm_blog'
              AND COLUMN_NAME = 'collaboration_epoch'
            """); var result = statement.executeQuery()) {
            return result.next() && "NO".equals(result.getString(1));
        }
    }

    private void acquireLock(Connection connection, String lock) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT GET_LOCK(?, 60)")) {
            statement.setString(1, lock);
            try (var result = statement.executeQuery()) {
                if (!result.next() || result.getInt(1) != 1) {
                    throw new SQLException("Timed out acquiring the blog schema migration lock");
                }
            }
        }
    }

    private void releaseLock(Connection connection, String lock) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            statement.setString(1, lock);
            try (var result = statement.executeQuery()) {
                if (!result.next() || result.getInt(1) != 1) {
                    throw new SQLException("Could not release the blog schema migration lock");
                }
            }
        }
    }
}
