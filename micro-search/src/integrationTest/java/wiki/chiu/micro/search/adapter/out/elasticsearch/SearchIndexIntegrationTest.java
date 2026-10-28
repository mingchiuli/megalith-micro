package wiki.chiu.micro.search.adapter.out.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.port.out.BlogIndexSource;
import wiki.chiu.micro.search.application.port.out.SearchRebuildControl;
import wiki.chiu.micro.search.application.service.IndexRebuildService;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

@Testcontainers(disabledWithoutDocker = true)
class SearchIndexIntegrationTest {

    @Container
    private static final GenericContainer<?> ELASTICSEARCH = elasticsearchContainer();

    private static GenericContainer<?> elasticsearchContainer() {
        GenericContainer<?> result = new GenericContainer<>(DockerImageName.parse("elasticsearch:9.5.1"));
        result.addEnv("discovery.type", "single-node");
        result.addEnv("xpack.security.enabled", "false");
        result.addEnv("xpack.ml.enabled", "false");
        result.addEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
        result.addExposedPort(9200);
        result.setWaitStrategy(Wait.forHttp("/").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(2)));
        return result;
    }

    private static AnnotationConfigApplicationContext context;
    private static ElasticsearchClient client;
    private static ElasticsearchIndicesClient indexClient;
    private static ElasticsearchTemplate template;
    private String alias;
    private ElasticsearchBlogAdapter blogs;
    private ElasticsearchIndexMaintenanceAdapter indexes;

    @BeforeAll
    static void connect() throws Exception {
        context = new AnnotationConfigApplicationContext(ElasticConfiguration.class);
        client = context.getBean(ElasticsearchClient.class);
        indexClient = client.indices();
        template = context.getBean(ElasticsearchTemplate.class);
        // Supply test analyzers for the production mapping's IK analyzer names.
        indexClient.putIndexTemplate(request -> request.name("test-search-analyzers")
            .indexPatterns("test-search-*")
            .template(value -> value.settings(settings -> settings.analysis(analysis -> analysis
                .analyzer("ik_smart", analyzer -> analyzer.standard(standard -> standard))
                .analyzer("ik_max_word", analyzer -> analyzer.standard(standard -> standard))))));
    }

    @AfterAll
    static void disconnect() {
        if (context != null) {
            context.close();
        }
    }

    @BeforeEach
    void createIsolatedIndex() throws Exception {
        alias = "test-search-" + UUID.randomUUID();
        indexes = new ElasticsearchIndexMaintenanceAdapter(client, template, alias);
        String target = indexes.createIndex();
        indexClient.updateAliases(request -> request.actions(action ->
            action.add(add -> add.index(target).alias(alias).isWriteIndex(true))));
        blogs = new ElasticsearchBlogAdapter(template, 5, alias);
    }

    @Test
    void statisticsDoNotBlockNewContentAndCannotResurrectDeletedDocuments() throws Exception {
        blogs.write(change(7, 10, 0, 0, "original"));
        for (int count = 1; count <= 20; count++) {
            blogs.updateReadCounts(List.of(new BlogReadCount(7, count)));
        }
        assertThat(client.get(request -> request.index(alias).id("7"), Map.class).version()).isGreaterThan(11);

        blogs.write(change(7, 11, 0, 2, "latest"));
        blogs.write(change(7, 10, 0, 1, "stale"));
        blogs.write(change(7, 11, 0, 1, "duplicate"));
        blogs.updateReadCounts(List.of(new BlogReadCount(7, 120), new BlogReadCount(99, 50)));
        blogs.updateReadCounts(List.of(new BlogReadCount(7, 100)));

        assertThat(document(7).getTitle()).isEqualTo("latest");
        assertThat(document(7).getRevision()).isEqualTo(11L);
        assertThat(document(7).getReadCount()).isEqualTo(120L);
        assertThat(document(99)).isNull();

        blogs.write(new BlogIndexChange(BlogIndexChange.Operation.REMOVE, 12, change(7, 12, 0, 120, "latest").blog()));
        blogs.updateReadCounts(List.of(new BlogReadCount(7, 200)));
        blogs.write(change(7, 11, 0, 120, "late event"));

        assertThat(document(7).getDeleted()).isTrue();
        assertThat(document(7).getRevision()).isEqualTo(12L);
        assertThat(document(7).getTitle()).isNull();
    }

    @Test
    void rebuildingRepairsMissingAndDeletedDocumentsAndRetainsTheOldIndex() {
        blogs.write(change(1, 1, 0, 3, "old"));
        blogs.write(change(2, 2, 0, 8, "deleted in database"));
        String previous = indexes.currentIndex();
        var service = new IndexRebuildService(source(List.of(
            change(1, 3, 0, 17, "fresh"), change(3, 1, 0, 0, "previously missing"))), indexes, control(), 1);

        var result = service.rebuild();

        assertThat(result.previousIndex()).isEqualTo(previous);
        assertThat(result.documents()).isEqualTo(2);
        assertThat(indexes.currentIndex()).isEqualTo(result.index());
        assertThat(document(1).getTitle()).isEqualTo("fresh");
        assertThat(document(2)).isNull();
        assertThat(document(3).getTitle()).isEqualTo("previously missing");
        assertThat(template.get("2", BlogDocument.class, IndexCoordinates.of(previous))).isNotNull();
    }

    @Test
    void aRejectedBulkDocumentDoesNotSwitchTheAlias() {
        blogs.write(change(7, 1, 0, 1, "old"));
        String previous = indexes.currentIndex();
        var service = new IndexRebuildService(source(List.of(change(7, 2, 1000, 2, "invalid byte status"))),
            indexes, control(), 500);

        assertThatThrownBy(service::rebuild).isInstanceOf(RuntimeException.class);

        assertThat(indexes.currentIndex()).isEqualTo(previous);
        assertThat(document(7).getTitle()).isEqualTo("old");
    }

    @Test
    void recommendationPagingUsesStableScoresAndPublicDocumentsOnly() throws Exception {
        for (long id = 1; id <= 7; id++) {
            blogs.write(change(id, 1, 0, id * 10, "article " + id, cover(id)));
        }
        blogs.write(change(8, 1, 2, 1000, "filtered article"));
        indexClient.refresh(request -> request.index(alias));

        var first = blogs.recommendationPage(1, 3);
        var second = blogs.recommendationPage(2, 3);
        var third = blogs.recommendationPage(3, 3);

        assertThat(first.content()).extracting(blog -> blog.id()).containsExactly(7L, 6L, 5L);
        assertThat(first.content()).extracting(blog -> blog.link())
            .containsExactly(cover(7), cover(6), cover(5));
        assertThat(second.content()).extracting(blog -> blog.id()).containsExactly(4L, 3L, 2L);
        assertThat(third.content()).extracting(blog -> blog.id()).containsExactly(1L);
        assertThat(first.totalElements()).isEqualTo(7);
        assertThat(first.last()).isFalse();
        assertThat(third.last()).isTrue();
    }

    @Test
    void publicSearchCardsCarryCoverLinks() throws Exception {
        blogs.write(change(1, 1, 0, 10, "searchable article", cover(1)));
        indexClient.refresh(request -> request.index(alias));

        var page = blogs.searchPublic(new PublicBlogSearchQuery(1, "searchable", false));

        assertThat(page.content()).extracting(hit -> hit.link()).containsExactly(cover(1));
    }

    @Test
    void publicSearchMarksEmptyAndExactFinalPagesAsLast() throws Exception {
        for (long id = 1; id <= 5; id++) {
            blogs.write(change(id, 1, 0, id, "searchable article " + id));
        }
        indexClient.refresh(request -> request.index(alias));

        var fullPage = blogs.searchPublic(new PublicBlogSearchQuery(1, "searchable", false));
        var emptyPage = blogs.searchPublic(new PublicBlogSearchQuery(1, "missing", false));

        assertThat(fullPage.content()).hasSize(5);
        assertThat(fullPage.last()).isTrue();
        assertThat(emptyPage.content()).isEmpty();
        assertThat(emptyPage.empty()).isTrue();
        assertThat(emptyPage.last()).isTrue();
    }

    @Test
    void adminRevisionGuardPreventsStaleOverwriteAndRecoveryAdvancesTheTombstone() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String adminIndex = "test-search-admin-" + suffix;
        String revisionIndex = "test-search-revision-" + suffix;
        createIndex(adminIndex, AdminBlogDocument.class);
        createIndex(revisionIndex, AdminRevisionDocument.class);
        var adapter =
            new ElasticsearchBlogAdapter(
                template, 5, alias, adminIndex, revisionIndex);
        adapter.writeAdmin(adminChange("UPDATE", 5, "latest", cover(7), 0));
        adapter.writeAdmin(adminChange(8L, 12L, "UPDATE", 1, "second", "", 0));
        indexClient.refresh(request -> request.index(adminIndex));
        AdminBlogDocument current =
            template.get("7", AdminBlogDocument.class, IndexCoordinates.of(adminIndex));

        adapter.applyAdminDocument(
            adminChange("UPDATE", 4, "stale", "", 0), current);
        indexClient.refresh(request -> request.index(adminIndex));
        assertThat(
                template.get("7", AdminBlogDocument.class, IndexCoordinates.of(adminIndex))
                    .getPublishedTitle())
            .isEqualTo("latest");

        adapter.writeAdmin(adminChange("RECOVER", 1, "recovered", cover(7), 1));
        indexClient.refresh(request -> request.index(adminIndex));
        AdminBlogDocument recovered =
            template.get("7", AdminBlogDocument.class, IndexCoordinates.of(adminIndex));
        assertThat(recovered.getPublishedTitle()).isEqualTo("recovered");
        assertThat(recovered.getAccessMode()).isEqualTo(1);
        assertThat(recovered.getAdminRevision()).isEqualTo(6L);
        assertThat(
                adapter.searchAdmin(
                        new PrivateBlogSearchQuery(
                            1, 10, null, null, null, null, 42L, true, false))
                    .content())
            .extracting(blog -> blog.id())
            .containsExactly(7L, 8L);
    }

    private BlogDocument document(long id) {
        return template.get(Long.toString(id), BlogDocument.class, IndexCoordinates.of(alias));
    }

    private void createIndex(String index, Class<?> documentType) {
        var operations = template.indexOps(IndexCoordinates.of(index));
        if (!operations.create(Map.of(), operations.createMapping(documentType))) {
            throw new IllegalStateException("test index creation failed: " + index);
        }
    }

    private static BlogAdminIndexChange adminChange(
        String operation, long revision, String title, String cover, int accessMode) {
        return adminChange(7L, 11L, operation, revision, title, cover, accessMode);
    }

    private static BlogAdminIndexChange adminChange(
        long blogId,
        long revisionId,
        String operation,
        long revision,
        String title,
        String cover,
        int accessMode) {
        LocalDateTime date = LocalDateTime.of(2026, 10, 28, 10, 0);
        BlogRevisionSnapshot revisionSnapshot =
            new BlogRevisionSnapshot(
                revisionId,
                blogId,
                1L,
                BlogRevisionOrigin.PUBLICATION,
                null,
                title,
                "description",
                "content",
                cover,
                BlogRenderMode.NORMAL,
                date,
                42L,
                date,
                List.of());
        return new BlogAdminIndexChange(
            operation,
            new BlogAdminSnapshot(
                blogId,
                42L,
                revisionId,
                date,
                null,
                accessMode,
                1L,
                revision,
                10L,
                date),
            new BlogDraftSnapshot(
                blogId,
                1L,
                revisionId,
                "draft",
                "description",
                "content",
                cover,
                BlogRenderMode.NORMAL,
                42L,
                date.plusHours(1),
                List.of()),
            revisionSnapshot);
    }

    private static BlogIndexChange change(long id, long revision, int status, long views, String title) {
        return change(id, revision, status, views, title, null);
    }

    private static BlogIndexChange change(
        long id, long revision, int status, long views, String title, String link) {
        var date = LocalDateTime.of(2026, 9, 1, 12, 0);
        return new BlogIndexChange(BlogIndexChange.Operation.UPDATE, revision,
            new BlogIndexEntry(id, 42L, status, views, title, "description", "content", link,
                date, date, null, null, null, null, null, null, List.of()));
    }

    private static String cover(long id) {
        return "https://cdn.example/cover-" + id + ".png";
    }

    private static BlogIndexSource source(List<BlogIndexChange> documents) {
        return new BlogIndexSource() {
            @Override
            public IndexSourceStatus status() {
                return new IndexSourceStatus(true, 0, 0, documents.size());
            }

            @Override
            public List<BlogIndexChange> snapshots(long afterId, int limit) {
                return documents.stream().filter(document -> document.blog().id() > afterId).limit(limit).toList();
            }
        };
    }

    private static SearchRebuildControl control() {
        return new SearchRebuildControl() {
            @Override
            public <T> T runExclusive(Supplier<T> task) {
                return task.get();
            }

            @Override
            public void requireQuiescent() {
            }
        };
    }

    @Configuration(proxyBeanMethods = false)
    static class ElasticConfiguration extends ElasticsearchConfiguration {

        @Override
        public ClientConfiguration clientConfiguration() {
            return ClientConfiguration.builder()
                .connectedTo(ELASTICSEARCH.getHost() + ":" + ELASTICSEARCH.getMappedPort(9200)).build();
        }
    }
}
