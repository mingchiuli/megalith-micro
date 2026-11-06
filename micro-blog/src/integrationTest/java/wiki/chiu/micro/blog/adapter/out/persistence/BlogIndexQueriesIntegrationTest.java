package wiki.chiu.micro.blog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.model.BlogDraftWriteCommand;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.outbox.adapter.out.persistence.OutboxStoreAdapter;
import wiki.chiu.micro.common.outbox.adapter.out.persistence.repository.OutboxEventRepository;
import wiki.chiu.micro.common.outbox.application.service.OutboxServiceImpl;

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
                    context.getBean(BlogDraftSensitiveContentRepository.class),
                    context.getBean(BlogRevisionSensitiveContentRepository.class));

            SqlCapture.clear();
            assertThat(adapter.findIdsAfter(0L, 1)).containsExactly(ids.getFirst());
            assertThat(SqlCapture.statements()).hasSize(1);
            assertThat(SqlCapture.statements().getFirst().split(" from ")[0])
                .contains(".id").doesNotContain("user_id", "read_count");
            assertThat(adapter.findIdsAfter(ids.getFirst(), 500)).containsExactly(ids.getLast());
            assertThat(adapter.findReadCountsAfter(0, 1))
                .containsExactly(new BlogReadCount(ids.get(0), 7));
            assertThat(adapter.findReadCountsAfter(ids.get(0), 500))
                .containsExactly(new BlogReadCount(ids.get(1), 0));
            var snapshot =
                adapter.findPublishedSnapshotsAfter(ids.get(0), 1).getFirst();
            assertThat(snapshot.id()).isEqualTo(ids.get(1));
            assertThat(snapshot.revision()).isEqualTo(11L);
            assertThat(snapshot.title()).isEqualTo("title-11");
            assertThat(adapter.findAdminSnapshotsAfter(0, 10))
                .hasSize(2)
                .allSatisfy(admin -> assertThat(admin.publishedRevision()).isNotNull());
            assertThat(adapter.findRevisionSnapshotsAfter(0, 10)).hasSize(2);
        }
    }

    @Test
    void deletionTargetsIncludeDraftOnlyBlogs() {
        try (var context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class)) {
            var blogs = context.getBean(BlogRepository.class);
            var drafts = context.getBean(BlogDraftRepository.class);
            var transaction =
                new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            long blogId =
                transaction.execute(
                    status -> {
                        BlogEntity blog = blogs.save(blog(0L, 0L));
                        drafts.save(
                            new BlogDraftEntity(
                                null,
                                blog.getId(),
                                1L,
                                null,
                                "draft",
                                "description",
                                "content",
                                "",
                                BlogRenderMode.NORMAL,
                                42L,
                                LocalDateTime.now()));
                        return blog.getId();
                    });
            var adapter =
                new JpaBlogQueryAdapter(
                    blogs,
                    context.getBean(BlogRevisionRepository.class),
                    drafts,
                    context.getBean(BlogDraftSensitiveContentRepository.class),
                    context.getBean(BlogRevisionSensitiveContentRepository.class));

            var target = adapter.findDeletionTargetsByIds(List.of(blogId)).getFirst();

            assertThat(target.blog().title()).isEqualTo("draft");
            assertThat(target.identity().adminRevision()).isZero();
        }
    }

    @Test
    void conditionalWritesRejectStaleRevisionsAndPublishWithoutReadingBack() {
        try (var context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class)) {
            var blogs = context.getBean(BlogRepository.class);
            var revisions = context.getBean(BlogRevisionRepository.class);
            var transaction =
                new TransactionTemplate(context.getBean(PlatformTransactionManager.class));

            transaction.executeWithoutResult(
                status -> {
                    BlogEntity blog = blogs.save(blog(0L, 0L));
                    BlogRevisionEntity revision = revisions.save(revision(blog, 1L));

                    assertThat(blogs.bumpAdminRevision(blog.getId(), 0L)).isEqualTo(1);
                    assertThat(blogs.bumpAdminRevision(blog.getId(), 0L)).isZero();
                    assertThat(
                            blogs.publish(
                                blog.getId(),
                                revision.getId(),
                                LocalDateTime.now(),
                                0,
                                0L,
                                1L))
                        .isEqualTo(1);
                    assertThat(
                            blogs.publish(
                                blog.getId(),
                                revision.getId(),
                                LocalDateTime.now(),
                                0,
                                0L,
                                1L))
                        .isZero();
                });
        }
    }

    private BlogEntity blog(long readCount, long revision) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return new BlogEntity(
            null, 42L, date, readCount, null, null, null, 0, revision, revision);
    }

    @Test
    void deletionAndExportReadInBatchesIncludingMissingPublishedVersions() {
        try (var context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class)) {
            var blogs = context.getBean(BlogRepository.class);
            var revisions = context.getBean(BlogRevisionRepository.class);
            var drafts = context.getBean(BlogDraftRepository.class);
            var sensitive = context.getBean(BlogRevisionSensitiveContentRepository.class);
            var transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            List<Long> ids = transaction.execute(status -> {
                List<Long> result = new ArrayList<>();
                for (int index = 0; index < 20; index++) {
                    BlogEntity blog = blogs.save(blog(0, 0));
                    result.add(blog.getId());
                    if (index % 2 == 0) {
                        BlogRevisionEntity revision = revisions.save(revision(blog, index));
                        blog.setPublishedRevisionId(revision.getId());
                        sensitive.save(new BlogRevisionSensitiveContentEntity(
                            null, revision.getId(), 1, 3, 3));
                    } else if (index == 1) {
                        blog.setPublishedRevisionId(-1L);
                    }
                    if (index != 19) {
                        drafts.save(new BlogDraftEntity(null, blog.getId(), 1L, null, "draft",
                            "description", "content", "", BlogRenderMode.NORMAL, 42L, LocalDateTime.now()));
                    }
                }
                return result;
            });
            var queries = queryAdapter(context);
            SqlCapture.clear();

            var targets = queries.findDeletionTargetsByIds(ids);

            assertThat(SqlCapture.statements()).hasSize(4).allMatch(sql -> sql.startsWith("select"));
            assertThat(targets).hasSize(20);
            assertThat(targets.getFirst().blog().title()).isEqualTo("title-0");
            assertThat(targets.getFirst().blog().sensitiveContent())
                .containsExactly(new BlogSensitiveSpan(1, 3, 3));
            assertThat(targets.get(1).blog().title()).isEqualTo("draft");
            assertThat(targets.getLast().blog().title()).isEmpty();
            SqlCapture.clear();
            assertThat(queries.findDeletionTargetsByUserIds(List.of(42L))).hasSize(20);
            assertThat(SqlCapture.statements()).hasSize(4);

            var reader = authoringReader(context);
            SqlCapture.clear();
            assertThat(reader.findIdentities(ids)).hasSize(20);
            assertThat(reader.findVersionsByBlogIds(ids)).hasSize(10)
                .allSatisfy(version -> assertThat(version.sensitiveContent()).hasSize(1));
            assertThat(SqlCapture.statements()).hasSize(3);
            SqlCapture.clear();
            assertThat(queries.findDeletionTargetsByIds(List.of())).isEmpty();
            assertThat(queries.findDeletionTargetsByUserIds(List.of())).isEmpty();
            assertThat(reader.findIdentities(List.of())).isEmpty();
            assertThat(reader.findVersionsByBlogIds(List.of())).isEmpty();
            assertThat(SqlCapture.statements()).isEmpty();
        }
    }

    @Test
    void deletionAndDraftReplacementDoNotSelectAndStaleDeletionRollsBackOutbox() {
        try (var context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class)) {
            var blogs = context.getBean(BlogRepository.class);
            var revisions = context.getBean(BlogRevisionRepository.class);
            var drafts = context.getBean(BlogDraftRepository.class);
            var draftSpans = context.getBean(BlogDraftSensitiveContentRepository.class);
            var revisionSpans = context.getBean(BlogRevisionSensitiveContentRepository.class);
            var outbox = context.getBean(OutboxEventRepository.class);
            var transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            var events = new OutboxServiceImpl(new OutboxStoreAdapter(outbox), event -> "{}");
            List<Long> ids = transaction.execute(status -> {
                List<Long> result = new ArrayList<>();
                for (int index = 0; index < 2; index++) {
                    BlogEntity blog = blogs.save(blog(0, 0));
                    result.add(blog.getId());
                    BlogRevisionEntity revision = revisions.save(revision(blog, index));
                    blog.setPublishedRevisionId(revision.getId());
                    revisionSpans.save(new BlogRevisionSensitiveContentEntity(null, revision.getId(), 1, 3, 3));
                    drafts.save(new BlogDraftEntity(null, blog.getId(), 1L, null, "draft", "description",
                        "content", "", BlogRenderMode.NORMAL, 42L, LocalDateTime.now()));
                    draftSpans.save(new BlogDraftSensitiveContentEntity(null, blog.getId(), 1, 3, 3));
                }
                return result;
            });
            var reader = authoringReader(context);
            var authoring = new JpaBlogAuthoringAdapter(blogs, revisions, drafts, revisionSpans, draftSpans, events);
            var identity = reader.findIdentity(ids.getFirst()).orElseThrow();
            var draft = reader.findDraft(ids.getFirst()).orElseThrow();
            SqlCapture.clear();
            transaction.executeWithoutResult(status -> authoring.saveDraft(new BlogDraftWriteCommand(
                identity, draft, new BlogContentCommand("new draft", "description", "content", "",
                    BlogRenderMode.NORMAL, List.of(new BlogSensitiveSpan(2, 4, 3))),
                null, 42L, LocalDateTime.now())));
            assertThat(SqlCapture.statements()).noneMatch(sql -> sql.startsWith("select"));
            assertThat(reader.findDraft(ids.getFirst()).orElseThrow().sensitiveContent())
                .containsExactly(new BlogSensitiveSpan(2, 4, 3));

            var targets = queryAdapter(context).findDeletionTargetsByIds(ids);
            transaction.executeWithoutResult(status -> blogs.bumpAdminRevision(ids.getLast(), 0L));
            long eventCount = outbox.count();
            var writer = new BlogWrapper(blogs, revisions, drafts, draftSpans, revisionSpans, events,
                new BlogMaintenanceMode(false));
            var event = new BlogEventContext(BlogOperateEnum.REMOVE, 42L);
            SqlCapture.clear();
            assertThatThrownBy(() -> transaction.executeWithoutResult(status -> writer.deleteByIds(targets, event)))
                .isInstanceOf(BaseException.class);
            assertThat(SqlCapture.statements()).noneMatch(sql -> sql.startsWith("select"));
            assertThat(blogs.count()).isEqualTo(2);
            assertThat(revisions.count()).isEqualTo(2);
            assertThat(drafts.count()).isEqualTo(2);
            assertThat(revisionSpans.count()).isEqualTo(2);
            assertThat(draftSpans.count()).isEqualTo(2);
            assertThat(outbox.count()).isEqualTo(eventCount);

            var current = queryAdapter(context).findDeletionTargetsByIds(ids);
            SqlCapture.clear();
            transaction.executeWithoutResult(status -> writer.deleteByIds(current, event));
            assertThat(SqlCapture.statements()).noneMatch(sql -> sql.startsWith("select"));
            assertThat(blogs.count()).isZero();
            assertThat(revisions.count()).isZero();
            assertThat(drafts.count()).isZero();
            assertThat(revisionSpans.count()).isZero();
            assertThat(draftSpans.count()).isZero();
            assertThat(outbox.count()).isEqualTo(eventCount + 4);
        }
    }

    @Test
    void recoveryInsertsOriginalIdWithoutSelectingAndDuplicateRecoveryRollsBack() {
        try (var context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class)) {
            var blogs = context.getBean(BlogRepository.class);
            var revisions = context.getBean(BlogRevisionRepository.class);
            var spans = context.getBean(BlogRevisionSensitiveContentRepository.class);
            var outbox = context.getBean(OutboxEventRepository.class);
            var transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            var writer = new BlogWrapper(blogs, revisions, context.getBean(BlogDraftRepository.class),
                context.getBean(BlogDraftSensitiveContentRepository.class), spans,
                new OutboxServiceImpl(new OutboxStoreAdapter(outbox), event -> "{}"),
                new BlogMaintenanceMode(false));
            var date = LocalDateTime.of(2026, 8, 1, 12, 0);
            Blog deleted = new Blog(700L, 42L, "recovered", "description", "content", date, date,
                1, "link", 9L, 3L, 8L, List.of(new BlogSensitiveSpan(1, 3, 3)));
            var event = new BlogEventContext(BlogOperateEnum.CREATE, 42L);
            SqlCapture.clear();

            transaction.executeWithoutResult(status -> writer.recoverDeletedBlog(deleted, event));

            assertThat(SqlCapture.statements()).noneMatch(sql -> sql.startsWith("select"));
            BlogEntity recovered = blogs.findById(700L).orElseThrow();
            assertThat(recovered.getCreated()).isEqualTo(date);
            assertThat(recovered.getReadCount()).isEqualTo(9L);
            assertThat(recovered.getPublicationRevision()).isEqualTo(3L);
            assertThat(recovered.getAdminRevision()).isEqualTo(8L);
            assertThat(recovered.getAccessMode()).isEqualTo(1);
            assertThat(queryAdapter(context).findById(700L).orElseThrow().sensitiveContent())
                .containsExactly(new BlogSensitiveSpan(1, 3, 3));
            assertThat(outbox.count()).isEqualTo(2);
            assertThatThrownBy(() -> transaction.executeWithoutResult(status -> writer.recoverDeletedBlog(deleted, event)))
                .isInstanceOf(RuntimeException.class);
            assertThat(revisions.count()).isEqualTo(1);
            assertThat(spans.count()).isEqualTo(1);
            assertThat(outbox.count()).isEqualTo(2);
        }
    }

    private JpaBlogQueryAdapter queryAdapter(AnnotationConfigApplicationContext context) {
        return new JpaBlogQueryAdapter(context.getBean(BlogRepository.class),
            context.getBean(BlogRevisionRepository.class), context.getBean(BlogDraftRepository.class),
            context.getBean(BlogDraftSensitiveContentRepository.class),
            context.getBean(BlogRevisionSensitiveContentRepository.class));
    }

    @Test
    void indexCleanupIsRepeatableRequiresUniqueIndexesAndRetainsIndexedLookups() throws Exception {
        try (var context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class)) {
            var dataSource = context.getBean(DataSource.class);
            var jdbc = new JdbcTemplate(dataSource);
            jdbc.execute("CREATE INDEX idx_blog_draft_blog ON m_blog_draft (blog_id)");
            jdbc.execute("CREATE INDEX idx_blog_revision_blog ON m_blog_revision (blog_id, version_no)");
            var script = new ClassPathResource("drop-redundant-blog-indexes.sql");
            try (var connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, script);
                ScriptUtils.executeSqlScript(connection, script);
            }
            assertThat(jdbc.queryForList("SHOW INDEX FROM m_blog_draft")).extracting(row -> row.get("Key_name"))
                .contains("uk_blog_draft_blog").doesNotContain("idx_blog_draft_blog");
            assertThat(jdbc.queryForList("SHOW INDEX FROM m_blog_revision")).extracting(row -> row.get("Key_name"))
                .contains("uk_blog_revision_version").doesNotContain("idx_blog_revision_blog");
            jdbc.execute("""
                INSERT INTO m_blog_revision
                    (blog_id, version_no, origin, title, description, content, render_mode, created_by, created_at)
                SELECT seq DIV 4, seq % 4, 'DRAFT', 'title', 'description', 'content', 'NORMAL', 42, NOW(6)
                FROM seq_1_to_20000
                """);
            jdbc.execute("""
                INSERT INTO m_blog_draft
                    (blog_id, draft_revision, title, description, content, render_mode, updated_by, updated_at)
                SELECT seq, 1, 'title', 'description', 'content', 'NORMAL', 42, NOW(6)
                FROM seq_1_to_20000
                """);
            jdbc.execute("ANALYZE TABLE m_blog_revision, m_blog_draft");
            var revisionPlan = jdbc.queryForList(
                "EXPLAIN SELECT id FROM m_blog_revision WHERE blog_id = 42 ORDER BY version_no DESC LIMIT 1");
            assertThat(revisionPlan).allSatisfy(row -> {
                assertThat(row.get("key")).isEqualTo("uk_blog_revision_version");
                assertThat(String.valueOf(row.get("Extra"))).doesNotContain("filesort");
            });
            assertThat(jdbc.queryForList("EXPLAIN SELECT id FROM m_blog_draft WHERE blog_id = 42"))
                .allSatisfy(row -> assertThat(row.get("key")).isEqualTo("uk_blog_draft_blog"));

            jdbc.execute("CREATE INDEX idx_blog_draft_blog ON m_blog_draft (blog_id)");
            jdbc.execute("CREATE INDEX idx_blog_revision_blog ON m_blog_revision (blog_id, version_no)");
            jdbc.execute("ALTER TABLE m_blog_draft DROP INDEX uk_blog_draft_blog");
            jdbc.execute("ALTER TABLE m_blog_revision DROP INDEX uk_blog_revision_version");
            try (var connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, script);
            }
            assertThat(jdbc.queryForList("SHOW INDEX FROM m_blog_draft")).extracting(row -> row.get("Key_name"))
                .contains("idx_blog_draft_blog");
            assertThat(jdbc.queryForList("SHOW INDEX FROM m_blog_revision")).extracting(row -> row.get("Key_name"))
                .contains("idx_blog_revision_blog");
        }
    }

    private JpaBlogAuthoringReader authoringReader(AnnotationConfigApplicationContext context) {
        return new JpaBlogAuthoringReader(context.getBean(BlogRepository.class),
            context.getBean(BlogRevisionRepository.class), context.getBean(BlogDraftRepository.class),
            context.getBean(BlogDraftSensitiveContentRepository.class),
            context.getBean(BlogRevisionSensitiveContentRepository.class));
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
    @EnableJpaRepositories(basePackageClasses = {BlogRepository.class, OutboxEventRepository.class})
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
            factory.setPackagesToScan("wiki.chiu.micro.blog.adapter.out.persistence.entity",
                "wiki.chiu.micro.common.outbox.adapter.out.persistence.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.session_factory.statement_inspector", SqlCapture.class.getName()));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }
}
