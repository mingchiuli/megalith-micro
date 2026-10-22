package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;

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
    void cursorQueriesUseOnlyTheCurrentPublishedRevision() {
        try (var context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class)) {
            var blogs = context.getBean(BlogRepository.class);
            var revisions = context.getBean(BlogRevisionRepository.class);
            var transaction =
                new TransactionTemplate(context.getBean(PlatformTransactionManager.class));

            var ids =
                transaction.execute(
                    status -> {
                        BlogEntity first = blogs.save(blog(7L, 10L));
                        BlogEntity second = blogs.save(blog(0L, 11L));
                        BlogRevisionEntity firstRevision = revisions.save(revision(first, 10L));
                        BlogRevisionEntity secondRevision = revisions.save(revision(second, 11L));
                        first.setPublishedRevisionId(firstRevision.getId());
                        second.setPublishedRevisionId(secondRevision.getId());
                        blogs.saveAll(List.of(first, second));
                        return List.of(first.getId(), second.getId());
                    });

            var adapter =
                new JpaBlogQueryAdapter(
                    blogs,
                    revisions,
                    context.getBean(BlogDraftRepository.class),
                    context.getBean(BlogRevisionSensitiveContentRepository.class));

            assertThat(adapter.findReadCountsAfter(0, 1))
                .containsExactly(new BlogReadCount(ids.get(0), 7));
            assertThat(adapter.findReadCountsAfter(ids.get(0), 500))
                .containsExactly(new BlogReadCount(ids.get(1), 0));
            var snapshot =
                adapter.findPublishedSnapshotsAfter(ids.get(0), 1).getFirst();
            assertThat(snapshot.id()).isEqualTo(ids.get(1));
            assertThat(snapshot.revision()).isEqualTo(11L);
            assertThat(snapshot.title()).isEqualTo("title-11");
        }
    }

    private BlogEntity blog(long readCount, long revision) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return new BlogEntity(
            null, 42L, date, readCount, null, null, null, 0, revision, revision);
    }

    private BlogRevisionEntity revision(BlogEntity blog, long revision) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return new BlogRevisionEntity(
            null,
            blog.getId(),
            1L,
            BlogRevisionOrigin.PUBLICATION,
            null,
            "title-" + revision,
            "description",
            "content",
            "",
            BlogRenderMode.NORMAL,
            date,
            42L,
            date);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableJpaRepositories(basePackageClasses = BlogRepository.class)
    static class PersistenceConfiguration {

        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                "jdbc:mariadb://"
                    + DATABASE.getHost()
                    + ":"
                    + DATABASE.getMappedPort(3306)
                    + "/blog_test",
                "root",
                "test-password");
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("wiki.chiu.micro.blog.adapter.out.persistence.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }
}
