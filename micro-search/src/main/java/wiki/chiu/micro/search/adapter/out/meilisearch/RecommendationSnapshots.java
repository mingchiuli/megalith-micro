package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.http.HttpMethod;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.RecommendationFeed;
import wiki.chiu.micro.search.application.model.RecommendationFeedQuery;
import wiki.chiu.micro.search.application.model.SearchIndexKind;

public final class RecommendationSnapshots {
    private final MeilisearchClient client;
    private final String index;
    private final RedissonClient redis;
    private final JsonMapper json;
    private final Duration ttl;

    public RecommendationSnapshots(MeilisearchClient client, MeilisearchIndexes indexes,
        RedissonClient redis, JsonMapper json, Duration ttl) {
        this.client = client;
        this.index = indexes.currentIndexes().get(SearchIndexKind.PUBLIC_BLOGS);
        this.redis = redis;
        this.json = json;
        this.ttl = ttl;
    }

    RecommendationFeed page(RecommendationFeedQuery query) {
        boolean restart = false;
        if (query.cursor() != null && !query.cursor().isBlank()) {
            var cursor = decode(query.cursor());
            if (cursor.version() == 2) {
                var rows = rows(cursor.snapshot());
                var slice = rows.range(cursor.offset(), Math.addExact(cursor.offset(), query.size()));
                if (!slice.isEmpty()) {
                    rows.expire(ttl);
                    return page(slice.stream().map(s -> json.readValue(s, PublicBlogRead.class)).toList(),
                        cursor, query.size(), false);
                }
            }
            restart = true;
        }

        var lock = redis.getReadWriteLock("megalith:search:" + index + ":snapshot").writeLock();
        lock.lock();
        try {
            client.drain(index);
            String generation = client.request(HttpMethod.GET, "/indexes/" + index, null).required("updatedAt").asString();
            var shared = redis.<String>getBucket("megalith:search:" + index + ":generation:" + generation, StringCodec.INSTANCE);
            String cached = shared.get();
            if (cached != null) {
                var cursor = new RecommendationCursor(2, UUID.fromString(cached), 0);
                var slice = rows(cursor.snapshot()).range(0, query.size());
                if (!slice.isEmpty()) {
                    return page(slice.stream().map(s -> json.readValue(s, PublicBlogRead.class)).toList(),
                        cursor, query.size(), restart);
                }
            }
            var snapshot = UUID.randomUUID();
            var ranked = readSnapshot();
            if (ranked.size() > query.size()) {
                var rows = rows(snapshot);
                for (int start = 0; start < ranked.size(); start += 128) {
                    var batch = redis.createBatch(org.redisson.api.BatchOptions.defaults()
                        .executionMode(org.redisson.api.BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC));
                    var chunk = batch.<String>getList(rows.getName(), StringCodec.INSTANCE);
                    chunk.addAllAsync(ranked.subList(start, Math.min(start + 128, ranked.size())).stream()
                        .map(json::writeValueAsString).toList());
                    chunk.expireAsync(ttl);
                    batch.execute();
                }
                shared.set(snapshot.toString(), ttl);
            }
            return page(ranked, new RecommendationCursor(2, snapshot, 0), query.size(), restart);
        } finally {
            lock.unlock();
        }
    }

    private List<PublicBlogRead> readSnapshot() {
        List<PublicBlogRead> result = new ArrayList<>();
        Instant origin = Instant.now();
        int offset = 0;
        while (true) {
            var docs = client.request(HttpMethod.POST, "/indexes/" + index + "/documents/fetch",
                Map.of("filter", "deleted = false AND status = " + BlogStatusEnum.NORMAL.getCode(),
                    "offset", offset, "limit", 256)).required("results");
            for (var doc : docs) {
                result.add(BlogDocumentConvertor.publicRead(json.treeToValue(doc, BlogDocument.class)));
            }
            if (docs.size() < 256) {
                break;
            }
            offset += docs.size();
        }
        result.sort(Comparator.<PublicBlogRead>comparingDouble(blog -> score(blog, origin)).reversed()
            .thenComparingLong(PublicBlogRead::id));
        return result;
    }

    private static double score(PublicBlogRead blog, Instant origin) {
        double days = blog.publishedAt() == null ? 0 : Math.abs(Duration.between(origin,
            blog.publishedAt().atZone(ZoneId.of("Asia/Shanghai")).toInstant()).toSeconds()) / 86400.0;
        double distance = Math.max(0, days - 30) / 180.0;
        return Math.log1p(blog.readCount() == null ? 0 : blog.readCount()) * Math.pow(0.5, distance * distance);
    }

    private RecommendationFeed page(List<PublicBlogRead> slice, RecommendationCursor cursor, int size, boolean restart) {
        boolean more = slice.size() > size;
        String next = more ? Base64.getUrlEncoder().withoutPadding().encodeToString(json.writeValueAsBytes(
            new RecommendationCursor(2, cursor.snapshot(), Math.addExact(cursor.offset(), size)))) : null;
        return new RecommendationFeed(slice.subList(0, Math.min(size, slice.size())), next, more, restart);
    }

    private RecommendationCursor decode(String value) {
        try {
            if (value.length() <= 4096) {
                var node = json.readTree(Base64.getUrlDecoder().decode(value));
                // A pre-migration PIT can be restarted using the existing restart response.
                if (node.path("version").asInt() == 1 && !node.path("pitId").asString("").isBlank()) {
                    return new RecommendationCursor(1, null, 0);
                }
                var cursor = json.treeToValue(node, RecommendationCursor.class);
                if (cursor.version() == 2 && cursor.snapshot() != null && cursor.offset() > 0
                    && cursor.offset() < Integer.MAX_VALUE - 1000) {
                    return cursor;
                }
            }
        } catch (RuntimeException invalid) {
            throw new IllegalArgumentException("invalid recommendation cursor", invalid);
        }
        throw new IllegalArgumentException("invalid recommendation cursor");
    }

    private RList<String> rows(UUID snapshot) {
        return redis.getList("megalith:search:" + index + ":feed:" + snapshot, StringCodec.INSTANCE);
    }
}
