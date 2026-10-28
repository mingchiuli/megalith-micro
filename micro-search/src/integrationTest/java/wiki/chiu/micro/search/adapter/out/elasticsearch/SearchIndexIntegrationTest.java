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
    void startupSchemaCreatesAllThreeAliasesIdempotently() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String publicAlias = "test-search-public-" + suffix;
        String adminAlias = "test-search-admin-" + suffix;
        String revisionAlias = "test-search-revision-" + suffix;
        var schema =
            new ElasticsearchSearchIndexSchemaAdapter(
                client, template, publicAlias, adminAlias, revisionAlias);

        schema.ensureAliases();
        String publicIndex =
            indexClient.getAlias(request -> request.name(publicAlias))
                .aliases()
                .keySet()
                .iterator()
                .next();
        schema.ensureAliases();

        assertThat(indexClient.existsAlias(request -> request.name(publicAlias)).value()).isTrue();
        assertThat(indexClient.existsAlias(request -> request.name(adminAlias)).value()).isTrue();
        assertThat(indexClient.existsAlias(request -> request.name(revisionAlias)).value()).isTrue();
        assertThat(
                indexClient.getAlias(request -> request.name(publicAlias))
                    .aliases()
                    .keySet())
            .containsExactly(publicIndex);
    }

    @Test
    void bootstrapWritesPopulateAdminAndRevisionIndexes() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String publicAlias = "test-search-public-" + suffix;
        String adminAlias = "test-search-admin-" + suffix;
        String revisionAlias = "test-search-revision-" + suffix;
        new ElasticsearchSearchIndexSchemaAdapter(
                client, template, publicAlias, adminAlias, revisionAlias)
            .ensureAliases();
        var adapter =
            new ElasticsearchBlogAdapter(
                template, 5, publicAlias, adminAlias, revisionAlias);
        LocalDateTime date = LocalDateTime.of(2026, 10, 28, 10, 0);
        BlogRevisionSnapshot published = revision(7L, 7L, date);
        BlogRevisionSnapshot historical = revision(9L, 7L, date);

        adapter.writeAdmin(
            new BlogAdminIndexChange(
                "BOOTSTRAP",
                new BlogAdminSnapshot(
                    7L, 42L, 7L, date, null, 0, 1L, 2L, 10L, date),
                new BlogDraftSnapshot(
                    7L,
                    1L,
                    7L,
                    "draft",
                    "description",
                    "content",
                    cover(7),
                    BlogRenderMode.NORMAL,
                    42L,
                    date.plusHours(1),
                    List.of()),
                published));
        adapter.writeRevision(
            new BlogAdminIndexChange("BOOTSTRAP", null, null, historical));
        indexClient.refresh(request -> request.index(adminAlias));
        indexClient.refresh(request -> request.index(revisionAlias));

        assertThat(
                template.get("7", AdminBlogDocument.class, IndexCoordinates.of(adminAlias))
                    .getPublishedLink())
            .isEqualTo(cover(7));
        assertThat(
                template.get("9", AdminRevisionDocument.class, IndexCoordinates.of(revisionAlias))
                    .getLink())
            .isEqualTo(cover(9));
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
    void repairRefreshesAnExistingDocumentAtTheSameRevision() throws Exception {
        blogs.write(change(1, 1, 0, 10, "article", ""));
        BlogIndexChange changed = change(1, 1, 0, 10, "article", cover(1));
        blogs.write(new BlogIndexChange(
            BlogIndexChange.Operation.REPAIR, changed.revision(), changed.blog()));
        indexClient.refresh(request -> request.index(alias));

        assertThat(document(1).getLink()).isEqualTo(cover(1));
    }

    @Test
    void bulkRepairCreatesMissingDocumentsAndRefreshesExistingOnes() throws Exception {
        blogs.write(change(1, 1, 0, 10, "article", ""));
        BlogIndexChange existing = change(1, 1, 0, 10, "article", cover(1));
        BlogIndexChange missing = change(2, 1, 0, 20, "new article", cover(2));
        blogs.writeAll(
            List.of(
                new BlogIndexChange(
                    BlogIndexChange.Operation.REPAIR, existing.revision(), existing.blog()),
                new BlogIndexChange(
                    BlogIndexChange.Operation.REPAIR, missing.revision(), missing.blog())));
        indexClient.refresh(request -> request.index(alias));

        assertThat(document(1).getLink()).isEqualTo(cover(1));
        assertThat(document(2).getLink()).isEqualTo(cover(2));
    }

    @Test
    void repairDoesNotResurrectADeletedDocumentAtTheSameRevision() {
        blogs.write(change(1, 1, 0, 10, "article", cover(1)));
        BlogIndexEntry deleted =
            new BlogIndexEntry(
                1L, 42L, 0, 10L, null, null, null, null, null);
        blogs.write(new BlogIndexChange(BlogIndexChange.Operation.REMOVE, 2, deleted));
        BlogIndexChange changed = change(1, 2, 0, 10, "article", cover(1));
        blogs.write(new BlogIndexChange(
            BlogIndexChange.Operation.REPAIR, changed.revision(), changed.blog()));

        assertThat(document(1).getDeleted()).isTrue();
    }

    @Test
    void relatedArticlesExcludeTheSourceAndDeletedDocuments() throws Exception {
        blogs.write(change(1, 1, 0, 10, "shared topic"));
        blogs.write(change(2, 1, 0, 20, "shared topic"));
        blogs.write(change(3, 1, 2, 30, "shared topic"));
        blogs.write(change(4, 1, 0, 40, "shared topic"));
        blogs.write(
            new BlogIndexChange(
                BlogIndexChange.Operation.REMOVE,
                2,
                change(4, 2, 0, 40, "shared topic").blog()));
        indexClient.refresh(request -> request.index(alias));

        var related = blogs.relatedArticles(1L, 10);

        assertThat(related)
            .extracting(blog -> blog.id())
            .containsExactlyInAnyOrder(2L, 3L);
    }

    private BlogDocument document(long id) {
        return template.get(Long.toString(id), BlogDocument.class, IndexCoordinates.of(alias));
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

    private static BlogRevisionSnapshot revision(long id, long blogId, LocalDateTime date) {
        return new BlogRevisionSnapshot(
            id,
            blogId,
            1L,
            BlogRevisionOrigin.PUBLICATION,
            null,
            "title",
            "description",
            "content",
            cover(id),
            BlogRenderMode.NORMAL,
            date,
            42L,
            date,
            List.of());
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
