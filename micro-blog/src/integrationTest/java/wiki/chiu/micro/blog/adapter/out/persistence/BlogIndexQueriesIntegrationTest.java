package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.domain.BlogEntity;

@Testcontainers(disabledWithoutDocker = true)
class BlogIndexQueriesIntegrationTest {

    @Container
    private static final GenericContainer<?> DATABASE = databaseContainer();

    private static GenericContainer<?> databaseContainer() {
        GenericContainer<?> result = new GenericContainer<>(DockerImageName.parse("mariadb:12.3.2"));
        result.addEnv("MARIADB_DATABASE", "blog_test");
        result.addEnv("MARIADB_ROOT_PASSWORD", "test-password");
        result.addExposedPort(3306);
        return result;
    }

    @Test
    void cursorQueriesProjectCountsAndIncludeRevisionWithoutLoadingOtherPages() {
        try (var context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class)) {
            var repository = context.getBean(BlogRepository.class);
            var transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            var first = blog(7L, 10L);
            var second = blog(null, 11L);
            transaction.executeWithoutResult(status -> repository.saveAll(List.of(first, second)));
            var adapter = new JpaBlogQueryAdapter(repository, context.getBean(BlogSensitiveContentRepository.class));

            assertThat(adapter.findReadCountsAfter(0, 1))
                .containsExactly(new BlogReadCount(first.getId(), 7));
            assertThat(adapter.findReadCountsAfter(first.getId(), 500))
                .containsExactly(new BlogReadCount(second.getId(), 0));
            var snapshot = adapter.findSnapshotsAfter(first.getId(), 1).getFirst();
            assertThat(snapshot.getId()).isEqualTo(second.getId());
            assertThat(snapshot.getEventRevision()).isEqualTo(11L);
            assertThat(adapter.findSnapshotsAfter(second.getId(), 1)).isEmpty();
        }
    }

    private BlogEntity blog(Long readCount, long revision) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return BlogEntity.builder().userId(42L).title("title").description("description").content("content")
            .status(0).created(date).updated(date).readCount(readCount).eventRevision(revision).build();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableJpaRepositories(basePackageClasses = BlogRepository.class)
    static class PersistenceConfiguration {

        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                "jdbc:mariadb://" + DATABASE.getHost() + ":" + DATABASE.getMappedPort(3306) + "/blog_test",
                "root", "test-password");
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("wiki.chiu.micro.blog.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(java.util.Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }
}
