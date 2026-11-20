package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.http.HttpMethod;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.SearchIndexKind;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;

public final class MeilisearchBlogIndexWriter implements BlogIndexWriter {
    private final MeilisearchClient client;
    private final RedissonClient redis;
    private final JsonMapper json;
    private final String publicIndex;
    private final String adminIndex;
    private final String revisionIndex;

    public MeilisearchBlogIndexWriter(MeilisearchClient client, MeilisearchIndexes indexes,
        RedissonClient redis, JsonMapper json) {
        this.client = client;
        this.redis = redis;
        this.json = json;
        publicIndex = indexes.currentIndexes().get(SearchIndexKind.PUBLIC_BLOGS);
        adminIndex = indexes.currentIndexes().get(SearchIndexKind.ADMIN_BLOGS);
        revisionIndex = indexes.currentIndexes().get(SearchIndexKind.REVISIONS);
    }

    private void serialize(String index, long id, Runnable write) {
        String key = "megalith:search:" + index + ":" + id;
        var lock = redis.getLock(key + ":lock");
        lock.lock();
        try {
            var pending = redis.getBucket(key + ":pending", StringCodec.INSTANCE);
            if (pending.isExists()) {
                client.drain(index);
            }
            pending.set("1");
            write.run();
            pending.delete();
        } finally {
            lock.unlock();
        }
    }

    private void publicWrite(long id, Runnable write) {
        var lock = redis.getReadWriteLock("megalith:search:" + publicIndex + ":snapshot").readLock();
        lock.lock();
        try {
            if (redis.getBucket(publicIndex + ":counts-pending", StringCodec.INSTANCE).isExists()) {
                client.drain(publicIndex);
            }
            serialize(publicIndex, id, write);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(BlogIndexChange change) {
        long id = change.blog().id();
        publicWrite(id, () -> {
            JsonNode current = client.document(publicIndex, id);
            if (current != null && current.required("revision").asLong() >= change.revision()) {
                return;
            }
            ObjectNode doc = json.valueToTree(BlogDocumentConvertor.publicDocument(change));
            if (current != null && change.operation() != BlogIndexChange.Operation.REMOVE) {
                doc.put("readCount", Math.max(doc.path("readCount").asLong(0),
                    current.path("readCount").asLong(0)));
            }
            client.replace(publicIndex, List.of(doc));
        });
    }

    @Override
    public void updateReadCounts(List<BlogReadCount> counts) {
        if (counts.isEmpty()) {
            return;
        }
        var requested = new HashMap<Long, Long>();
        for (BlogReadCount count : counts) {
            requested.merge(count.blogId(), count.readCount(), Math::max);
        }
        var lock = redis.getReadWriteLock("megalith:search:" + publicIndex + ":snapshot").writeLock();
        lock.lock();
        try {
            client.drain(publicIndex);
            var pending = redis.getBucket(publicIndex + ":counts-pending", StringCodec.INSTANCE);
            var existing = client.request(HttpMethod.POST, "/indexes/" + publicIndex + "/documents/fetch",
                Map.of("ids", requested.keySet(), "filter", "deleted = false", "limit", requested.size(),
                    "fields", List.of("id", "readCount"))).required("results");
            var updates = new ArrayList<Map<String, Long>>();
            for (JsonNode row : existing) {
                long id = row.required("id").asLong();
                long count = requested.get(id);
                if (row.path("readCount").asLong(0) < count) {
                    updates.add(Map.of("id", id, "readCount", count));
                }
            }
            if (!updates.isEmpty()) {
                pending.set("1");
                client.task(HttpMethod.PUT, "/indexes/" + publicIndex + "/documents", updates);
            }
            pending.delete();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void writeAdmin(BlogAdminIndexChange change) {
        serialize(adminIndex, change.blog().id(), () -> {
            writeRevision(change.revision());
            if (!Objects.equals(change.revision(), change.publishedRevision())) {
                writeRevision(change.publishedRevision());
            }
            var node = client.document(adminIndex, change.blog().id());
            AdminBlogDocument current = node == null ? null : json.treeToValue(node, AdminBlogDocument.class);
            if (current != null && (current.adminRevision() > change.blog().adminRevision()
                || (current.adminRevision().equals(change.blog().adminRevision())
                    && !"RECOVER".equals(change.operation())))) {
                return;
            }
            client.replace(adminIndex, List.of(BlogDocumentConvertor.adminProjection(current, change)));
        });
    }

    private void writeRevision(BlogRevisionSnapshot revision) {
        if (revision != null) {
            client.replace(revisionIndex, List.of(BlogDocumentConvertor.revisionDocument(revision)));
        }
    }
}
