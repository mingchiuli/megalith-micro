package wiki.chiu.micro.user.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;

/**
 * Applies the canonical administration metadata after Hibernate has created the tables.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminMetadataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminMetadataInitializer.class);
    private static final String LOCK_NAME = "megalith:user:admin-metadata";
    private static final String SCRIPT = "script/admin-metadata.sql";

    private final DataSource dataSource;
    private final AuthCacheEvictionOutbox cacheEvictions;

    public AdminMetadataInitializer(
        DataSource dataSource, AuthCacheEvictionOutbox cacheEvictions) {
        this.dataSource = dataSource;
        this.cacheEvictions = cacheEvictions;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            if (!acquireLock(connection)) {
                throw new IllegalStateException("admin metadata lock is held by another replica");
            }
            try {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource(SCRIPT));
                long adminRoleId = findAdminRoleId(connection);
                cacheEvictions.enqueue(List.of(), List.of(adminRoleId), List.of("admin"), true, true);
                log.info("Canonical administration metadata is current");
            } finally {
                releaseLock(connection);
            }
        } catch (SQLException failure) {
            throw new IllegalStateException("admin metadata initialization failed", failure);
        }
    }

    private long findAdminRoleId(Connection connection) throws SQLException {
        try (var statement =
                 connection.prepareStatement("SELECT id FROM m_role WHERE code = 'admin'");
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                throw new IllegalStateException("admin role was not created");
            }
            return result.getLong(1);
        }
    }

    private boolean acquireLock(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT GET_LOCK(?, 30)")) {
            statement.setString(1, LOCK_NAME);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) == 1;
            }
        }
    }

    private void releaseLock(Connection connection) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            statement.setString(1, LOCK_NAME);
            statement.executeQuery();
        }
    }
}
