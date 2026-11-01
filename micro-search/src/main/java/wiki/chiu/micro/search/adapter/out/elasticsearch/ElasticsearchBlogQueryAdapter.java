package wiki.chiu.micro.search.adapter.out.elasticsearch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RecommendationFeed;
import wiki.chiu.micro.search.application.model.RecommendationFeedQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;

public final class ElasticsearchBlogQueryAdapter implements BlogSearchIndex {

    private static final Logger log =
        LoggerFactory.getLogger(ElasticsearchBlogQueryAdapter.class);

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final int publicPageSize;
    private final String indexName;
    private final String adminIndexName;
    private final String revisionIndexName;
    private final Duration pitKeepAlive;
    private final RecommendationCursorCodec cursorCodec;
    private final Counter pitOpened;
    private final Counter pitClosed;
    private final Counter pitRestarted;
    private final Counter pitFailures;

    public ElasticsearchBlogQueryAdapter(
        ElasticsearchTemplate elasticsearchTemplate,
        int publicPageSize,
        String indexName,
        String adminIndexName,
        String revisionIndexName) {
        this(
            elasticsearchTemplate,
            publicPageSize,
            indexName,
            adminIndexName,
            revisionIndexName,
            Duration.ofMinutes(2),
            JsonMapper.builder().build(),
            new SimpleMeterRegistry());
    }

    public ElasticsearchBlogQueryAdapter(
        ElasticsearchTemplate elasticsearchTemplate,
        int publicPageSize,
        String indexName,
        String adminIndexName,
        String revisionIndexName,
        Duration pitKeepAlive,
        JsonMapper jsonMapper,
        MeterRegistry meterRegistry) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.publicPageSize = publicPageSize;
        this.indexName = indexName;
        this.adminIndexName = adminIndexName;
        this.revisionIndexName = revisionIndexName;
        this.pitKeepAlive = pitKeepAlive;
        this.cursorCodec = new RecommendationCursorCodec(jsonMapper);
        this.pitOpened = meterRegistry.counter("megalith.search.recommendation.pit.opened");
        this.pitClosed = meterRegistry.counter("megalith.search.recommendation.pit.closed");
        this.pitRestarted =
            meterRegistry.counter("megalith.search.recommendation.pit.restart");
        this.pitFailures =
            meterRegistry.counter("megalith.search.recommendation.pit.failures");
    }

    @Override
    public Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query) {
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(
                PublicSearchQueryConvertor.searchConvert(
                    query.keywords(), query.page(), publicPageSize, query.allInfo()),
                BlogDocument.class,
                IndexCoordinates.of(indexName));
        return toPage(hits, query.page(), publicPageSize);
    }

    @Override
    public Optional<PublicBlogRead> findPublic(long blogId) {
        BlogDocument document =
            elasticsearchTemplate.get(
                Long.toString(blogId), BlogDocument.class, IndexCoordinates.of(indexName));
        if (document == null || Boolean.TRUE.equals(document.getDeleted())) {
            return Optional.empty();
        }
        return Optional.of(BlogDocumentMapper.publicRead(document));
    }

    @Override
    public RecommendationFeed recommendationFeed(RecommendationFeedQuery query) {
        RecommendationCursor cursor =
            query.cursor() == null || query.cursor().isBlank()
                ? null
                : cursorCodec.decode(query.cursor());
        try {
            return recommendationFeed(cursor, query.size(), false);
        } catch (RuntimeException failure) {
            if (cursor != null && isExpiredPointInTime(failure)) {
                pitRestarted.increment();
                return recommendationFeed(null, query.size(), true);
            }
            throw failure;
        }
    }

    @Override
    public List<PublicBlogRead> recommendationHead(int limit) {
        NativeQuery query =
            BlogSearchQueryFactory.recommendation(Instant.now(), null, limit);
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(
                query, BlogDocument.class, IndexCoordinates.of(indexName));
        return hits.getSearchHits().stream()
            .limit(limit)
            .map(hit -> BlogDocumentMapper.publicRead(hit.getContent()))
            .toList();
    }

    @Override
    public Page<AdminBlogRead> searchAdmin(PrivateBlogSearchQuery query) {
        SearchHits<@NonNull AdminBlogDocument> hits =
            elasticsearchTemplate.search(
                BlogSearchQueryFactory.admin(query),
                AdminBlogDocument.class,
                IndexCoordinates.of(adminIndexName));
        boolean draftMode =
            query.draftOnly()
                || Objects.equals(query.status(), BlogStatusEnum.DRAFT.getCode());
        int totalPages = pageCount(hits.getTotalHits(), query.pageSize());
        return new Page<>(
            hits.getSearchHits().stream()
                .map(hit -> BlogDocumentMapper.adminRead(hit.getContent(), draftMode))
                .toList(),
            hits.getTotalHits(),
            query.page(),
            query.pageSize(),
            query.page() == 1,
            hits.getTotalHits() == 0 || query.page() >= totalPages,
            hits.getTotalHits() == 0,
            totalPages);
    }

    @Override
    public Page<AdminRevisionRead> searchRevisions(RevisionSearchQuery query) {
        AdminBlogDocument blog =
            elasticsearchTemplate.get(
                Long.toString(query.blogId()),
                AdminBlogDocument.class,
                IndexCoordinates.of(adminIndexName));
        Long currentPublishedRevisionId =
            blog == null ? null : blog.getPublishedRevisionId();
        SearchHits<@NonNull AdminRevisionDocument> hits =
            elasticsearchTemplate.search(
                BlogSearchQueryFactory.revision(query),
                AdminRevisionDocument.class,
                IndexCoordinates.of(revisionIndexName));
        int totalPages = pageCount(hits.getTotalHits(), query.pageSize());
        return new Page<>(
            hits.getSearchHits().stream()
                .map(
                    hit ->
                        BlogDocumentMapper.revisionRead(
                            hit.getContent(), currentPublishedRevisionId))
                .toList(),
            hits.getTotalHits(),
            query.page(),
            query.pageSize(),
            query.page() == 1,
            hits.getTotalHits() == 0 || query.page() >= totalPages,
            hits.getTotalHits() == 0,
            totalPages);
    }

    private static Page<BlogSearchHit> toPage(
        SearchHits<@NonNull BlogDocument> hits, int page, int pageSize) {
        long totalElements = hits.getTotalHits();
        int totalPages = pageCount(totalElements, pageSize);
        return new Page<>(
            hits.getSearchHits().stream()
                .map(
                    hit -> {
                        BlogDocument document = hit.getContent();
                        return new BlogSearchHit(
                            document.getId(),
                            document.getUserId(),
                            document.getStatus(),
                            document.getTitle(),
                            document.getDescription(),
                            document.getContent(),
                            document.getLink(),
                            document.getCreated().toLocalDateTime(),
                            hit.getScore(),
                            hit.getHighlightFields());
                    })
                .toList(),
            totalElements,
            page,
            pageSize,
            page == 1,
            totalElements == 0 || page >= totalPages,
            totalElements == 0,
            totalPages);
    }

    private static int pageCount(long total, int pageSize) {
        return (int) ((total + pageSize - 1) / pageSize);
    }

    private RecommendationFeed recommendationFeed(
        RecommendationCursor cursor, int size, boolean restart) {
        boolean opened = cursor == null;
        String pitId =
            opened
                ? elasticsearchTemplate.openPointInTime(
                    IndexCoordinates.of(indexName), pitKeepAlive, false)
                : cursor.pitId();
        Instant snapshotTime =
            opened ? Instant.now() : Instant.ofEpochMilli(cursor.snapshotEpochMillis());
        List<Object> searchAfter =
            cursor == null ? null : List.of(cursor.score(), cursor.id());
        if (opened) {
            pitOpened.increment();
        }

        NativeQuery query =
            BlogSearchQueryFactory.recommendation(snapshotTime, searchAfter, size + 1);
        query.setPointInTime(new Query.PointInTime(pitId, pitKeepAlive));
        SearchHits<@NonNull BlogDocument> hits;
        try {
            hits =
                elasticsearchTemplate.search(
                    query, BlogDocument.class, IndexCoordinates.of(indexName));
        } catch (RuntimeException failure) {
            if (opened) {
                closePointInTime(pitId);
            }
            pitFailures.increment();
            throw failure;
        }

        List<SearchHit<@NonNull BlogDocument>> searchHits = hits.getSearchHits();
        if (searchHits.isEmpty()) {
            closePointInTime(pitId);
            return new RecommendationFeed(List.of(), null, false, restart);
        }

        boolean hasMore = searchHits.size() > size;
        List<SearchHit<@NonNull BlogDocument>> returned =
            searchHits.subList(0, Math.min(size, searchHits.size()));
        SearchHit<@NonNull BlogDocument> last = returned.getLast();
        String nextCursor =
            hasMore
                ? cursorCodec.encode(
                    new RecommendationCursor(
                        1,
                        pitId,
                        snapshotTime.toEpochMilli(),
                        sortScore(last),
                        sortId(last)))
                : null;
        if (!hasMore) {
            closePointInTime(pitId);
        }
        return new RecommendationFeed(
            returned.stream()
                .map(hit -> BlogDocumentMapper.publicRead(hit.getContent()))
                .toList(),
            nextCursor,
            hasMore,
            restart);
    }

    private static double sortScore(SearchHit<@NonNull BlogDocument> hit) {
        if (hit.getSortValues().size() < 2
            || !(hit.getSortValues().getFirst() instanceof Number score)) {
            throw new IllegalStateException("recommendation score is missing from search sort");
        }
        return score.doubleValue();
    }

    private static long sortId(SearchHit<@NonNull BlogDocument> hit) {
        if (hit.getSortValues().size() < 2 || !(hit.getSortValues().get(1) instanceof Number id)) {
            throw new IllegalStateException("recommendation id is missing from search sort");
        }
        return id.longValue();
    }

    private void closePointInTime(String pitId) {
        try {
            if (Boolean.TRUE.equals(elasticsearchTemplate.closePointInTime(pitId))) {
                pitClosed.increment();
            }
        } catch (RuntimeException failure) {
            log.warn("Failed to close recommendation PIT", failure);
        }
    }

    private static boolean isExpiredPointInTime(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof ResourceNotFoundException
                || (current.getMessage() != null
                && (current.getMessage().contains("search_context_missing_exception")
                    || current.getMessage().contains("No search context found")
                    || current.getMessage().contains("no search context found")))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
