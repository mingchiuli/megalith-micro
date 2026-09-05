package wiki.chiu.micro.search.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.IndexRebuildRejectedException;
import wiki.chiu.micro.search.application.port.out.BlogIndexMaintenance;

public final class ElasticsearchIndexMaintenanceAdapter implements BlogIndexMaintenance {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexMaintenanceAdapter.class);
    private final ElasticsearchIndicesClient indices;
    private final ElasticsearchTemplate template;
    private final String alias;
    private final String legacyIndex;

    public ElasticsearchIndexMaintenanceAdapter(
        ElasticsearchClient client, ElasticsearchTemplate template, String alias, String legacyIndex) {
        if (!alias.matches("[a-z0-9][a-z0-9_-]*")) {
            throw new IllegalArgumentException("invalid search index alias");
        }
        this.indices = client.indices();
        this.template = template;
        this.alias = alias;
        this.legacyIndex = legacyIndex;
    }

    public void ensureAlias() {
        try {
            if (indices.existsAlias(request -> request.name(alias)).value()) {
                currentIndex();
                return;
            }
            if (indices.exists(request -> request.index(alias)).value()) {
                throw new IllegalStateException("search alias name is occupied by a physical index: " + alias);
            }
            String target = indices.exists(request -> request.index(legacyIndex)).value()
                ? legacyIndex : createIndex();
            var result = indices.updateAliases(request -> request.actions(action ->
                action.add(add -> add.index(target).alias(alias).isWriteIndex(true))));
            if (!result.acknowledged()) {
                throw new IllegalStateException("search alias initialization was not acknowledged");
            }
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    @Override
    public String currentIndex() {
        try {
            var targets = indices.getAlias(request -> request.name(alias)).aliases().keySet();
            if (targets.size() != 1) {
                throw new IndexRebuildRejectedException("search alias must point to exactly one index");
            }
            return targets.iterator().next();
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    @Override
    public String createIndex() {
        String target = alias + "-v5-" + UUID.randomUUID();
        var operations = template.indexOps(IndexCoordinates.of(target));
        if (!operations.create(Map.of(), operations.createMapping(BlogDocument.class))) {
            throw new IllegalStateException("search index creation failed: " + target);
        }
        log.info("Created search rebuild target {}", target);
        return target;
    }

    @Override
    public void writeSnapshots(String index, List<BlogIndexChange> snapshots) {
        List<IndexQuery> queries = snapshots.stream().map(snapshot -> IndexQuery.builder()
            .withId(snapshot.blog().id().toString())
            .withObject(ElasticsearchBlogAdapter.toDocument(snapshot))
            .build()).toList();
        if (!queries.isEmpty()) {
            template.bulkIndex(queries, IndexCoordinates.of(index));
        }
    }

    @Override
    public long refreshAndCount(String index) {
        var coordinates = IndexCoordinates.of(index);
        template.indexOps(coordinates).refresh();
        return template.count(NativeQuery.builder().withQuery(query -> query.matchAll(match -> match)).build(),
            BlogDocument.class, coordinates);
    }

    @Override
    public void activate(String expectedPreviousIndex, String index) {
        if (!currentIndex().equals(expectedPreviousIndex)) {
            throw new IndexRebuildRejectedException("search alias changed during rebuild");
        }
        try {
            var result = indices.updateAliases(request -> request
                .actions(action -> action.remove(remove -> remove.index(expectedPreviousIndex).alias(alias)))
                .actions(action -> action.add(add -> add.index(index).alias(alias).isWriteIndex(true))));
            if (!result.acknowledged()) {
                throw new IllegalStateException("search alias switch was not acknowledged; inspect alias before retrying");
            }
            log.info("Switched search alias {} from {} to {}", alias, expectedPreviousIndex, index);
        } catch (IOException failure) {
            throw new UncheckedIOException("Search alias switch outcome is unknown; inspect alias before retrying", failure);
        }
    }
}
