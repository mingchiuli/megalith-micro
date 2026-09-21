package wiki.chiu.micro.user.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
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

import wiki.chiu.micro.common.outbox.adapter.out.persistence.repository.OutboxEventRepository;
import wiki.chiu.micro.common.outbox.application.OutboxStore;
import wiki.chiu.micro.common.outbox.domain.OutboxEventEntity;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;
import wiki.chiu.micro.user.adapter.out.persistence.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.MenuRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleMenuRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRoleRepository;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;
import wiki.chiu.micro.user.domain.MenuEntity;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.RoleMenuEntity;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.user.domain.UserRoleEntity;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Testcontainers(disabledWithoutDocker = true)
class PersistenceQueryIntegrationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 19, 12, 0);

    @Container
    private static final GenericContainer<?> DATABASE = databaseContainer();

    private static AnnotationConfigApplicationContext context;
    private static JdbcTemplate jdbc;
    private static TransactionTemplate transaction;
    private static OutboxEventRepository outbox;
    private static OutboxStore outboxStore;
    private static RoleRepository roles;
    private static UserRoleRepository userRoles;
    private static RoleMenuRepository roleMenus;
    private static RoleDataPermissionRepository dataPermissions;
    private static MenuRepository menus;
    private static MenuAuthorityRepository menuAuthorities;

    private static GenericContainer<?> databaseContainer() {
        GenericContainer<?> result = new GenericContainer<>(DockerImageName.parse("mariadb:12.3.2"));
        result.addEnv("MARIADB_DATABASE", "user_test");
        result.addEnv("MARIADB_ROOT_PASSWORD", "test-password");
        result.addExposedPort(3306);
        return result;
    }

    @BeforeAll
    static void startContext() {
        context = new AnnotationConfigApplicationContext(PersistenceConfiguration.class);
        jdbc = new JdbcTemplate(context.getBean(DataSource.class));
        transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        outbox = context.getBean(OutboxEventRepository.class);
        outboxStore = new OutboxStore(outbox);
        roles = context.getBean(RoleRepository.class);
        userRoles = context.getBean(UserRoleRepository.class);
        roleMenus = context.getBean(RoleMenuRepository.class);
        dataPermissions = context.getBean(RoleDataPermissionRepository.class);
        menus = context.getBean(MenuRepository.class);
        menuAuthorities = context.getBean(MenuAuthorityRepository.class);
    }

    @AfterAll
    static void stopContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void outboxSchemaDeclaresDedicatedIndexes() {
        List<String> indexNames =
            jdbc.query("SHOW INDEX FROM m_outbox_event", (rs, rowNum) -> rs.getString("Key_name"));

        assertThat(indexNames)
            .contains("idx_outbox_poll", "idx_outbox_aggregate", "idx_outbox_manage");
    }

    @Test
    void outboxPollingPlanAvoidsFullTableScans() {
        seedBacklog();
        LocalDateTime polledAt = NOW.plusDays(1);

        SqlCapture.clear();
        outbox.findReady(OutboxProducer.BLOG, polledAt, PageRequest.ofSize(50));
        String sql = SqlCapture.lastMatching(statement -> statement.toLowerCase().contains("m_outbox_event"));

        List<PlanRow> plan = explain(sql, OutboxProducer.BLOG.name(), polledAt, 50);

        assertThat(plan).noneMatch(row -> "ALL".equalsIgnoreCase(row.type()));
        assertThat(plan.stream().map(PlanRow::key).toList())
            .contains("idx_outbox_poll", "idx_outbox_aggregate");
    }

    @Test
    void outboxManagementLookupUsesEventIndex() {
        seedBacklog();

        SqlCapture.clear();
        outboxStore.manage("missing-event", OutboxProducer.BLOG, "pause");
        String sql =
            SqlCapture.lastMatching(
                statement ->
                    statement.toLowerCase().startsWith("update")
                        && statement.toLowerCase().contains("m_outbox_event"));

        List<PlanRow> plan = explain(sql, "missing-event", OutboxProducer.BLOG.name());

        assertThat(plan).noneMatch(row -> "ALL".equalsIgnoreCase(row.type()));
        assertThat(plan.stream().map(PlanRow::key).toList()).contains("idx_outbox_manage");
    }

    @Test
    void earlierPendingEventBlocksLaterReadyEventsOfTheSameAggregate() {
        jdbc.update("DELETE FROM m_outbox_event WHERE producer = ?", OutboxProducer.USER.name());
        transaction.executeWithoutResult(
            status -> {
                outbox.save(event("blocked-1", OutboxProducer.USER, "blocked", "PAUSED"));
                outbox.save(event("blocked-2", OutboxProducer.USER, "blocked", "READY"));
                outbox.save(event("free-1", OutboxProducer.USER, "free", "READY"));
                outbox.save(event("paused-only", OutboxProducer.USER, "paused", "PAUSED"));
            });

        List<OutboxEventEntity> ready =
            outbox.findReady(OutboxProducer.USER, NOW.plusDays(1), PageRequest.ofSize(50));

        assertThat(ready.stream().map(OutboxEventEntity::getEventId).toList())
            .containsExactly("free-1");
    }

    @Test
    void roleDeletionRemovesEveryRelatedRowThroughBulkDeletes() {
        Long firstRole = roles.save(role("A")).getId();
        Long secondRole = roles.save(role("B")).getId();
        Long untouchedRole = roles.save(role("C")).getId();
        transaction.executeWithoutResult(
            status -> {
                userRoles.saveAll(
                    List.of(
                        userRole(1L, firstRole),
                        userRole(2L, secondRole),
                        userRole(3L, untouchedRole)));
                roleMenus.saveAll(
                    List.of(roleMenu(firstRole, 11L), roleMenu(secondRole, 12L), roleMenu(untouchedRole, 13L)));
                dataPermissions.saveAll(
                    List.of(
                        dataPermission(firstRole),
                        dataPermission(secondRole),
                        dataPermission(untouchedRole)));
            });

        var wrapper =
            new RoleWrapper(
                roles, roleMenus, userRoles, dataPermissions, mock(AuthCacheEvictionOutbox.class));
        transaction.executeWithoutResult(
            status -> wrapper.delete(List.of(firstRole, secondRole), List.of("A", "B")));

        assertThat(count("SELECT COUNT(*) FROM m_role WHERE id IN (?, ?)", firstRole, secondRole)).isZero();
        assertThat(
                count(
                    "SELECT COUNT(*) FROM m_user_role WHERE role_id IN (?, ?)", firstRole, secondRole))
            .isZero();
        assertThat(
                count(
                    "SELECT COUNT(*) FROM m_role_menu WHERE role_id IN (?, ?)", firstRole, secondRole))
            .isZero();
        assertThat(
                count(
                    "SELECT COUNT(*) FROM m_role_data_permission WHERE role_id IN (?, ?)",
                    firstRole,
                    secondRole))
            .isZero();
        assertThat(count("SELECT COUNT(*) FROM m_role WHERE id = ?", untouchedRole)).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM m_user_role WHERE role_id = ?", untouchedRole))
            .isEqualTo(1);
    }

    @Test
    void roleMenuReplacementDeletesOldRowsAndStoresNewOnes() {
        Long roleId = roles.save(role("D")).getId();
        transaction.executeWithoutResult(
            status -> roleMenus.saveAll(List.of(roleMenu(roleId, 21L))));
        var wrapper = new RoleMenuWrapper(roleMenus, mock(AuthCacheEvictionOutbox.class));

        transaction.executeWithoutResult(
            status -> wrapper.saveMenu(roleId, "D", List.of(roleMenu(roleId, 22L))));

        List<Long> menuIds =
            jdbc.queryForList(
                "SELECT menu_id FROM m_role_menu WHERE role_id = ?", Long.class, roleId);
        assertThat(menuIds).containsExactly(22L);
    }

    @Test
    void menuDeletionFlushesPendingEntityRemovalBeforeBulkDeletes() {
        Long menuId = menus.save(menu(101L, "delete-me")).getId();
        Long roleId = roles.save(role("E")).getId();
        transaction.executeWithoutResult(
            status -> {
                roleMenus.saveAll(List.of(roleMenu(roleId, menuId)));
                menuAuthorities.saveAll(List.of(new MenuAuthorityEntity(null, menuId, 55L, null, null)));
            });

        var wrapper =
            new RoleMenuAuthorityWrapper(
                menus, menuAuthorities, roleMenus, mock(AuthCacheEvictionOutbox.class));
        transaction.executeWithoutResult(
            status -> wrapper.deleteMenu(menuId, List.of(roleId), List.of("E")));

        assertThat(count("SELECT COUNT(*) FROM m_menu WHERE id = ?", menuId)).isZero();
        assertThat(count("SELECT COUNT(*) FROM m_role_menu WHERE menu_id = ?", menuId)).isZero();
        assertThat(count("SELECT COUNT(*) FROM m_menu_authority WHERE menu_id = ?", menuId)).isZero();
    }

    private void seedBacklog() {
        Integer existing =
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM m_outbox_event WHERE producer = ?",
                Integer.class,
                OutboxProducer.BLOG.name());
        if (existing != null && existing > 0) {
            return;
        }
        jdbc.execute(
            """
                INSERT INTO m_outbox_event
                    (event_id, producer, aggregate_type, aggregate_id, event_type, payload,
                     state, attempts, available_at, created_at)
                SELECT CONCAT('seed-', seq), 'BLOG', 'BLOG', CONCAT('agg-', seq DIV 4),
                       'BlogChangedMessage', '{}', IF(seq % 97 = 0, 'PAUSED', 'READY'), 0,
                       NOW(6), NOW(6)
                FROM seq_1_to_20000
                """);
        jdbc.execute("ANALYZE TABLE m_outbox_event");
    }

    private List<PlanRow> explain(String sql, Object... parameters) {
        return jdbc.query(
            "EXPLAIN " + sql,
            statement -> {
                for (int index = 0; index < parameters.length; index++) {
                    statement.setObject(index + 1, parameters[index]);
                }
            },
            (rs, rowNum) ->
                new PlanRow(
                    rs.getString("select_type"),
                    rs.getString("table"),
                    rs.getString("type"),
                    rs.getString("key")));
    }

    private int count(String sql, Object... parameters) {
        Integer value = jdbc.queryForObject(sql, Integer.class, parameters);
        return value == null ? 0 : value;
    }

    private static OutboxEventEntity event(
        String eventId, OutboxProducer producer, String aggregateId, String state) {
        var entity = new OutboxEventEntity();
        entity.setEventId(eventId);
        entity.setProducer(producer);
        entity.setAggregateType("BLOG");
        entity.setAggregateId(aggregateId);
        entity.setEventType("BlogChangedMessage");
        entity.setPayload("{}");
        entity.setState(state);
        entity.setAttempts(0);
        entity.setAvailableAt(NOW);
        entity.setCreatedAt(NOW);
        return entity;
    }

    private static RoleEntity role(String code) {
        return new RoleEntity(null, "role-" + code, code, "remark", NOW, NOW, 1);
    }

    private static UserRoleEntity userRole(Long userId, Long roleId) {
        return new UserRoleEntity(null, userId, roleId, NOW, NOW);
    }

    private static RoleMenuEntity roleMenu(Long roleId, Long menuId) {
        return new RoleMenuEntity(null, roleId, menuId, NOW, NOW);
    }

    private static RoleDataPermissionEntity dataPermission(Long roleId) {
        return new RoleDataPermissionEntity(roleId, DataPermissionEnum.BLOG_VIEW_ALL);
    }

    private static MenuEntity menu(Long parentId, String name) {
        return new MenuEntity(
            null, parentId, "title-" + name, name, "/url", "comp", 1, "icon", 1, 1, NOW, NOW);
    }

    private record PlanRow(String selectType, String table, String type, String key) {
    }

    @Configuration(proxyBeanMethods = false)
    @EnableJpaRepositories(
        basePackages = {
            "wiki.chiu.micro.user.adapter.out.persistence.repository",
            "wiki.chiu.micro.common.outbox.adapter.out.persistence.repository"
        })
    static class PersistenceConfiguration {

        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                "jdbc:mariadb://"
                    + DATABASE.getHost()
                    + ":"
                    + DATABASE.getMappedPort(3306)
                    + "/user_test",
                "root",
                "test-password");
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(
                "wiki.chiu.micro.user.domain", "wiki.chiu.micro.common.outbox.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(
                Map.of(
                    "hibernate.hbm2ddl.auto",
                    "create-drop",
                    "hibernate.session_factory.statement_inspector",
                    SqlCapture.class.getName()));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }
}
