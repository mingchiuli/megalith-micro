package wiki.chiu.micro.search.adapter.out.meilisearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RecommendationFeedQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;
import wiki.chiu.micro.search.application.model.SearchIndexKind;
import wiki.chiu.micro.search.application.port.out.BlogIndexSource;
import wiki.chiu.micro.search.application.port.out.SearchRebuildControl;
import wiki.chiu.micro.search.application.service.IndexRebuildService;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

@Testcontainers(disabledWithoutDocker = true)
class SearchIndexIntegrationTest {

    @Container
    private static final GenericContainer<?> MEILI = new GenericContainer<>("getmeili/meilisearch:v1.57.0");
    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine");

    static {
        MEILI.addExposedPort(7700);
        MEILI.setWaitStrategy(Wait.forHttp("/health"));
        REDIS.addExposedPort(6379);
    }

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static RedissonClient redis;
    private MeilisearchClient client;
    private String alias;
    private String adminAlias;
    private String revisionAlias;
    private MeilisearchBlogQueryAdapter queries;
    private MeilisearchBlogIndexWriter writer;
    private MeilisearchIndexes indexes;

    @BeforeAll
    static void connect() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        redis = Redisson.create(config);
    }

    @AfterAll
    static void disconnect() {
        if (redis != null) {
            redis.shutdown();
        }
    }

    @BeforeEach
    void createIsolatedIndexes() {
        String suffix = UUID.randomUUID().toString();
        alias = "blogs-" + suffix;
        adminAlias = "admin-" + suffix;
        revisionAlias = "rev-" + suffix;
        var http = RestClient.builder().baseUrl("http://" + MEILI.getHost() + ":" + MEILI.getMappedPort(7700)).build();
        client = new MeilisearchClient(http, JSON, Duration.ofMinutes(2));
        indexes = new MeilisearchIndexes(client, alias, adminAlias, revisionAlias, 10000);
        indexes.initialize();
        var snapshots = new RecommendationSnapshots(client, indexes, redis, JSON, Duration.ofMinutes(2));
        queries = new MeilisearchBlogQueryAdapter(client, indexes, snapshots, JSON, 5);
        writer = new MeilisearchBlogIndexWriter(client, indexes, redis, JSON);
    }

    @Test
    void statisticsDoNotBlockNewContentAndCannotResurrectDeletedDocuments() throws Exception {
        writer.write(change(7, 10, 0, 0, "original"));
        for (int count = 1; count <= 20; count++) {
            writer.updateReadCounts(List.of(new BlogReadCount(7, count)));
        }

        writer.write(change(7, 11, 0, 2, "latest"));
        writer.write(change(7, 10, 0, 1, "stale"));
        writer.write(change(7, 11, 0, 1, "duplicate"));
        writer.updateReadCounts(List.of(new BlogReadCount(7, 120), new BlogReadCount(99, 50)));
        writer.updateReadCounts(List.of(new BlogReadCount(7, 100)));

        assertThat(document(7).title()).isEqualTo("latest");
        assertThat(document(7).revision()).isEqualTo(11L);
        assertThat(document(7).readCount()).isEqualTo(120L);
        assertThat(document(99)).isNull();

        writer.write(new BlogIndexChange(BlogIndexChange.Operation.REMOVE, 12, change(7, 12, 0, 120, "latest").blog()));
        writer.updateReadCounts(List.of(new BlogReadCount(7, 200)));
        writer.write(change(7, 11, 0, 120, "late event"));

        assertThat(document(7).deleted()).isTrue();
        assertThat(document(7).revision()).isEqualTo(12L);
        assertThat(document(7).title()).isNull();
    }

    @Test
    void rebuildingRepairsMissingAndDeletedDocumentsAndRetainsTheOldIndex() {
        writer.write(change(1, 1, 0, 3, "old"));
        writer.write(change(2, 2, 0, 8, "deleted in database"));
        String previous = indexes.currentIndexes().get(SearchIndexKind.PUBLIC_BLOGS);
        var service = new IndexRebuildService(source(List.of(
            change(1, 3, 0, 17, "fresh"), change(3, 1, 0, 0, "previously missing"))), indexes, control(), 1);

        var result = service.rebuild();

        var publicResult = result.indexes().getFirst();
        assertThat(publicResult.index()).isEqualTo(previous);
        assertThat(publicResult.documents()).isEqualTo(2);
        assertThat(indexes.currentIndexes().get(SearchIndexKind.PUBLIC_BLOGS))
            .isEqualTo(publicResult.index());
        assertThat(document(1).title()).isEqualTo("fresh");
        assertThat(document(2)).isNull();
        assertThat(document(3).title()).isEqualTo("previously missing");
        assertThat(client.document(publicResult.previousIndex(), 2)).isNotNull();
    }

    @Test
    void aRejectedSourceRevisionDoesNotSwitchTheLiveIndex() {
        writer.write(change(7, 1, 0, 1, "old"));
        String previous = indexes.currentIndexes().get(SearchIndexKind.PUBLIC_BLOGS);
        var service = new IndexRebuildService(source(List.of(change(7, 0, 0, 2, "invalid revision"))),
            indexes, control(), 500);

        assertThatThrownBy(service::rebuild).isInstanceOf(RuntimeException.class);

        assertThat(indexes.currentIndexes().get(SearchIndexKind.PUBLIC_BLOGS))
            .isEqualTo(previous);
        assertThat(document(7).title()).isEqualTo("old");
    }

    @Test
    void recommendationPagingUsesStableScoresAndPublicDocumentsOnly() throws Exception {
        for (long id = 1; id <= 7; id++) {
            writer.write(change(id, 1, 0, id * 10, "article " + id, cover(id)));
        }
        writer.write(change(8, 1, 2, 1000, "filtered article"));

        var first = queries.recommendationFeed(new RecommendationFeedQuery(null, 3));
        var second =
            queries.recommendationFeed(
                new RecommendationFeedQuery(first.nextCursor(), 3));
        var third =
            queries.recommendationFeed(
                new RecommendationFeedQuery(second.nextCursor(), 3));

        assertThat(first.content()).extracting(blog -> blog.id()).containsExactly(7L, 6L, 5L);
        assertThat(first.content()).extracting(blog -> blog.link())
            .containsExactly(cover(7), cover(6), cover(5));
        assertThat(second.content()).extracting(blog -> blog.id()).containsExactly(4L, 3L, 2L);
        assertThat(third.content()).extracting(blog -> blog.id()).containsExactly(1L);
        assertThat(first.hasMore()).isTrue();
        assertThat(third.hasMore()).isFalse();
        assertThat(third.nextCursor()).isNull();
    }

    @Test
    void recommendationCursorKeepsTheInitialSnapshotWhileTheIndexChanges() throws Exception {
        for (long id = 1; id <= 5; id++) {
            writer.write(change(id, 1, 0, id * 10, "article " + id));
        }

        var first = queries.recommendationFeed(new RecommendationFeedQuery(null, 2));
        Long firstPageLastId = first.content().getLast().id();

        writer.updateReadCounts(List.of(new BlogReadCount(1, 1_000)));
        writer.write(change(6, 1, 0, 1_000, "new article"));
        writer.write(
            new BlogIndexChange(
                BlogIndexChange.Operation.REMOVE,
                2,
                change(firstPageLastId, 2, 0, 0, "deleted").blog()));

        var collected = new ArrayList<Long>();
        first.content().forEach(blog -> collected.add(blog.id()));
        var next = first;
        while (next.hasMore()) {
            next =
                queries.recommendationFeed(
                    new RecommendationFeedQuery(next.nextCursor(), 2));
            next.content().forEach(blog -> collected.add(blog.id()));
        }

        assertThat(collected).containsExactly(5L, 4L, 3L, 2L, 1L);
        assertThat(collected).doesNotContain(6L);
    }

    @Test
    void expiredRecommendationCursorRestartsFromANewSnapshot() throws Exception {
        for (long id = 1; id <= 4; id++) {
            writer.write(change(id, 1, 0, id * 10, "article " + id));
        }
        var first = queries.recommendationFeed(new RecommendationFeedQuery(null, 2));
        var cursor = JSON.readValue(java.util.Base64.getUrlDecoder().decode(first.nextCursor()), RecommendationCursor.class);
        redis.getKeys().delete("megalith:search:" + alias + ":feed:" + cursor.snapshot());

        var restarted =
            queries.recommendationFeed(
                new RecommendationFeedQuery(first.nextCursor(), 2));

        assertThat(restarted.restart()).isTrue();
        assertThat(restarted.content()).extracting(blog -> blog.id())
            .containsExactly(4L, 3L);
        assertThat(restarted.nextCursor()).isNotBlank();
    }

    @Test
    void publicSearchCardsCarryCoverLinks() throws Exception {
        writer.write(change(1, 1, 0, 10, "searchable article", cover(1)));

        var page = queries.searchPublic(new PublicBlogSearchQuery(1, "searchable", false));

        assertThat(page.content()).extracting(hit -> hit.link()).containsExactly(cover(1));
    }

    @Test
    void publicSearchMarksEmptyAndExactFinalPagesAsLast() throws Exception {
        for (long id = 1; id <= 5; id++) {
            writer.write(change(id, 1, 0, id, "searchable article " + id));
        }

        var fullPage = queries.searchPublic(new PublicBlogSearchQuery(1, "searchable", false));
        var emptyPage = queries.searchPublic(new PublicBlogSearchQuery(1, "missing", false));

        assertThat(fullPage.content()).hasSize(5);
        assertThat(fullPage.last()).isTrue();
        assertThat(emptyPage.content()).isEmpty();
        assertThat(emptyPage.empty()).isTrue();
        assertThat(emptyPage.last()).isTrue();
    }

    @Test
    void adminPaginationCoversResultsBeyondTheEngineDefaultLimit() {
        var rows = LongStream.rangeClosed(1, 1005)
            .mapToObj(id -> adminChange(id, id, "UPDATE", 1, "检索", "", 0)).toList();
        indexes.writeAdminSnapshots(adminAlias, rows);
        var page = queries.searchAdmin(new PrivateBlogSearchQuery(101, 10, null, null, null, null, 42, false));
        assertThat(page.totalElements()).isEqualTo(1005);
        assertThat(page.content()).extracting(row -> row.id()).containsExactly(1001L, 1002L, 1003L, 1004L, 1005L);
        assertThat(page.last()).isTrue();
    }

    @Test
    void reportsFailuresFromTheAsyncDocumentTask() {
        assertThatThrownBy(() -> client.replace(alias, List.of(java.util.Map.of("title", "missing primary key"))))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("failed");
        assertThat(indexes.refreshAndCount(SearchIndexKind.PUBLIC_BLOGS, alias)).isZero();
    }

    @Test
    void repeatedInitializationPreservesDocumentsAndConcurrentCountsNeverRegress() throws Exception {
        writer.write(change(7, 1, 0, 1, "缓存失效"));
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Void>> updates = LongStream.rangeClosed(1, 12).mapToObj(count -> (Callable<Void>) () -> {
                writer.updateReadCounts(List.of(new BlogReadCount(7, count)));
                return null;
            }).toList();
            for (var write : workers.invokeAll(updates)) {
                write.get();
            }
        }
        indexes.initialize();
        assertThat(document(7).readCount()).isEqualTo(12);
        var result = queries.searchPublic(new PublicBlogSearchQuery(1, "缓存", true));
        assertThat(result.content()).singleElement().satisfies(hit -> {
            assertThat(hit.title()).isEqualTo("缓存失效");
            assertThat(hit.highlight()).containsKey("title");
        });
    }

    @Test
    void invalidCursorIsRejected() {
        assertThatThrownBy(() -> queries.recommendationFeed(new RecommendationFeedQuery("not-base64", 3)))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("invalid recommendation cursor");
    }

    @Test
    void handlesOutOfOrderAdminEventsAndScopesDraftAndHistoryQueries() {
        writer.writeAdmin(adminChange("UPDATE", 5, "已发布文章", cover(7), 0));
        writer.writeAdmin(adminChange("UPDATE", 4, "旧标题", "", 0));
        writer.writeAdmin(adminChange("RECOVER", 6, "恢复后的文章", cover(7), 1));
        writer.writeAdmin(adminChange("UPDATE", 5, "旧事件", "", 0));
        var admin = queries.searchAdmin(new PrivateBlogSearchQuery(1, 10, "恢复", null, null, null, 42L, false));
        assertThat(admin.content()).singleElement().satisfies(row -> {
            assertThat(row.title()).isEqualTo("恢复后的文章");
            assertThat(row.adminRevision()).isEqualTo(6);
        });
        assertThat(queries.searchAdmin(new PrivateBlogSearchQuery(1, 10, null, null, null, null, 41L, false)).content()).isEmpty();
        assertThat(queries.searchAdmin(new PrivateBlogSearchQuery(1, 10, "draft", null, null, null, 42L, false, true)).content()).hasSize(1);
        assertThat(queries.searchRevisions(new RevisionSearchQuery(7, 1, 10, null, 42, false)).content()).hasSize(1);
        assertThat(queries.searchRevisions(new RevisionSearchQuery(7, 1, 10, null, 41, false)).content()).isEmpty();
    }

    @Test
    void newerDraftEventBeforePublicationKeepsPublishedContentAndRevision() throws Exception {
        LocalDateTime date = LocalDateTime.of(2026, 10, 28, 10, 0);
        BlogRevisionSnapshot publishedRevision =
            new BlogRevisionSnapshot(
                11L,
                7L,
                1L,
                BlogRevisionOrigin.PUBLICATION,
                null,
                "published content",
                "description",
                "body",
                cover(7),
                BlogRenderMode.NORMAL,
                date,
                42L,
                date,
                List.of());
        BlogAdminSnapshot newerBlog =
            new BlogAdminSnapshot(
                7L, 42L, 11L, date, null, 0, 1L, 8L, 10L, date);
        BlogDraftSnapshot newerDraft =
            new BlogDraftSnapshot(
                7L,
                2L,
                11L,
                "new draft",
                "description",
                "body",
                cover(7),
                BlogRenderMode.NORMAL,
                42L,
                date.plusMinutes(1),
                List.of());

        writer.writeAdmin(
            new BlogAdminIndexChange(
                "DRAFT_UPSERT",
                newerBlog,
                newerDraft,
                null,
                publishedRevision));
        writer.writeAdmin(adminChange("PUBLICATION_UPSERT", 7, "stale publication", cover(7), 0));

        AdminBlogDocument current =
            JSON.treeToValue(client.document(adminAlias, 7), AdminBlogDocument.class);
        assertThat(current.adminRevision()).isEqualTo(8L);
        assertThat(current.publishedRevisionId()).isEqualTo(11L);
        assertThat(current.publishedTitle()).isEqualTo("published content");
        assertThat(
                client.document(revisionAlias, 11))
            .isNotNull();
    }

    private BlogDocument document(long id) {
        var node = client.document(alias, id);
        return node == null ? null : JSON.treeToValue(node, BlogDocument.class);
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
            revisionSnapshot,
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

            @Override
            public List<BlogAdminIndexChange> adminSnapshots(
                long afterId, int limit) {
                return documents.stream().filter(document -> document.blog().id() > afterId).limit(limit)
                    .map(document -> adminChange(document.blog().id(), document.revision(), "PUBLICATION_UPSERT",
                        document.blog().id() * 10, document.blog().title(), document.blog().link(), 0)).toList();
            }

            @Override
            public List<BlogRevisionIndexEntry> revisionSnapshots(long afterRevisionId, int limit) {
                return List.of();
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

}
