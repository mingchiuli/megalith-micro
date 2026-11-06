package wiki.chiu.micro.blog.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BlogSchemaMigrationConfigTest {

    @Test
    void databaseFailureStopsStartupInsteadOfSkippingMigration() throws SQLException {
        var dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("database unavailable"));
        new ApplicationContextRunner()
            .withUserConfiguration(BlogSchemaMigrationConfig.class)
            .withBean(DataSource.class, () -> dataSource)
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(SQLException.class)
                    .hasStackTraceContaining("database unavailable");
            });
    }
}
