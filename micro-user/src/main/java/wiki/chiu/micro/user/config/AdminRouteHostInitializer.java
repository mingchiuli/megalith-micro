package wiki.chiu.micro.user.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;

/**
 * Keeps gateway route targets aligned with the current deployment's service names.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminRouteHostInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminRouteHostInitializer.class);
    private static final String UPDATE_HOST =
        """
        UPDATE m_authority
        SET service_host = ?
        WHERE status = 0
          AND service_port = ?
          AND (service_host IS NULL OR service_host <> ?)
        """;

    private final DataSource dataSource;
    private final AuthCacheEvictionOutbox cacheEvictions;
    private final AdminRouteHostProperties properties;

    public AdminRouteHostInitializer(
        DataSource dataSource,
        AuthCacheEvictionOutbox cacheEvictions,
        AdminRouteHostProperties properties) {
        this.dataSource = dataSource;
        this.cacheEvictions = cacheEvictions;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        AdminRouteHostProperties.Hosts hosts = properties.getHosts();
        List<RouteHost> routeHosts =
            List.of(
                new RouteHost(8081, requireHost(hosts.getAuth(), "auth")),
                new RouteHost(8086, requireHost(hosts.getUser(), "user")),
                new RouteHost(8082, requireHost(hosts.getBlog(), "blog")),
                new RouteHost(8089, requireHost(hosts.getSync(), "sync")),
                new RouteHost(8083, requireHost(hosts.getExhibit(), "exhibit")),
                new RouteHost(8085, requireHost(hosts.getSearch(), "search")));
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int changed = updateHosts(connection, routeHosts);
                connection.commit();
                if (changed > 0) {
                    cacheEvictions.enqueue(List.of(), List.of(), List.of(), false, true);
                    log.info("Updated {} administration route hosts", changed);
                }
            } catch (SQLException failure) {
                connection.rollback();
                throw failure;
            }
        } catch (SQLException failure) {
            throw new IllegalStateException("administration route host initialization failed", failure);
        }
    }

    private int updateHosts(Connection connection, List<RouteHost> routeHosts)
        throws SQLException {
        int changed = 0;
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_HOST)) {
            for (RouteHost routeHost : routeHosts) {
                statement.setString(1, routeHost.host());
                statement.setInt(2, routeHost.port());
                statement.setString(3, routeHost.host());
                changed += statement.executeUpdate();
            }
        }
        return changed;
    }

    private String requireHost(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("admin route host is missing: " + name);
        }
        return value;
    }

    private record RouteHost(int port, String host) {
    }
}
