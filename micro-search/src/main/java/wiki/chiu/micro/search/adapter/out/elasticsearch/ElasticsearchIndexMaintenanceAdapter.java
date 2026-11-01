package wiki.chiu.micro.search.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;
import wiki.chiu.micro.search.application.model.IndexRebuildRejectedException;
import wiki.chiu.micro.search.application.model.IndexTransition;
import wiki.chiu.micro.search.application.model.SearchIndexKind;
import wiki.chiu.micro.search.application.port.out.BlogIndexMaintenance;

public final class ElasticsearchIndexMaintenanceAdapter implements BlogIndexMaintenance {

    private static final Logger log =
        LoggerFactory.getLogger(ElasticsearchIndexMaintenanceAdapter.class);

    private final ElasticsearchIndicesClient indices;
    private final ElasticsearchTemplate template;
    private final Map<SearchIndexKind, String> aliases;
    private final Map<SearchIndexKind, Class<?>> documentTypes;

    public ElasticsearchIndexMaintenanceAdapter(
        ElasticsearchClient client,
        ElasticsearchTemplate template,
        String publicAlias,
        String adminAlias,
        String revisionAlias) {
        this.indices = client.indices();
        this.template = template;
        this.aliases = new EnumMap<>(SearchIndexKind.class);
        aliases.put(SearchIndexKind.PUBLIC_BLOGS, requireAlias(publicAlias));
        aliases.put(SearchIndexKind.ADMIN_BLOGS, requireAlias(adminAlias));
        aliases.put(SearchIndexKind.REVISIONS, requireAlias(revisionAlias));
        this.documentTypes =
            Map.of(
                SearchIndexKind.PUBLIC_BLOGS, BlogDocument.class,
                SearchIndexKind.ADMIN_BLOGS, AdminBlogDocument.class,
                SearchIndexKind.REVISIONS, AdminRevisionDocument.class);
    }

    @Override
    public Map<SearchIndexKind, String> currentIndexes() {
        Map<SearchIndexKind, String> current = new EnumMap<>(SearchIndexKind.class);
        aliases.forEach((kind, alias) -> current.put(kind, currentIndex(kind, alias)));
        return Map.copyOf(current);
    }

    @Override
    public String createIndex(SearchIndexKind kind) {
        String target = aliases.get(kind) + "-v1-" + UUID.randomUUID();
        var operations = template.indexOps(IndexCoordinates.of(target));
        if (!operations.create(Map.of(), operations.createMapping(documentTypes.get(kind)))) {
            throw new IllegalStateException("search index creation failed: " + target);
        }
        log.info("Created search rebuild target {} for {}", target, kind);
        return target;
    }

    @Override
    public void writePublicSnapshots(String index, List<BlogIndexChange> snapshots) {
        write(
            index,
            snapshots.stream()
                .map(
                    snapshot ->
                        IndexQuery.builder()
                            .withId(snapshot.blog().id().toString())
                            .withObject(BlogDocumentMapper.publicDocument(snapshot))
                            .build())
                .toList());
    }

    @Override
    public void writeAdminSnapshots(String index, List<BlogAdminIndexChange> snapshots) {
        write(
            index,
            snapshots.stream()
                .map(
                    snapshot ->
                        IndexQuery.builder()
                            .withId(snapshot.blog().id().toString())
                            .withObject(BlogDocumentMapper.adminDocument(snapshot))
                            .build())
                .toList());
    }

    @Override
    public void writeRevisionSnapshots(
        String index, List<BlogRevisionIndexEntry> revisions) {
        write(
            index,
            revisions.stream()
                .map(
                    revision ->
                        IndexQuery.builder()
                            .withId(revision.id().toString())
                            .withObject(
                                BlogDocumentMapper.revisionDocument(
                                    new BlogRevisionSnapshot(
                                        revision.id(),
                                        revision.blogId(),
                                        revision.versionNo(),
                                        revision.origin(),
                                        revision.baseRevisionId(),
                                        revision.title(),
                                        revision.description(),
                                        revision.content(),
                                        revision.link(),
                                        revision.renderMode(),
                                        revision.firstPublishedAt(),
                                        revision.createdBy(),
                                        revision.createdAt(),
                                        revision.sensitiveContent())))
                            .build())
                .toList());
    }

    @Override
    public long refreshAndCount(SearchIndexKind kind, String index) {
        var coordinates = IndexCoordinates.of(index);
        template.indexOps(coordinates).refresh();
        return template.count(
            NativeQuery.builder()
                .withQuery(query -> query.matchAll(match -> match))
                .build(),
            documentTypes.get(kind),
            coordinates);
    }

    @Override
    public void activate(List<IndexTransition> transitions) {
        Map<SearchIndexKind, String> current = currentIndexes();
        for (IndexTransition transition : transitions) {
            if (!transition.previousIndex().equals(current.get(transition.kind()))) {
                throw new IndexRebuildRejectedException(
                    "search alias changed during rebuild: " + transition.kind());
            }
        }
        try {
            var request =
                indices
                    .updateAliases(
                        update -> {
                            for (IndexTransition transition : transitions) {
                                update.actions(
                                    action ->
                                        action.remove(
                                            remove ->
                                                remove
                                                    .index(transition.previousIndex())
                                                    .alias(aliases.get(transition.kind()))));
                                update.actions(
                                    action ->
                                        action.add(
                                            add ->
                                                add.index(transition.index())
                                                    .alias(aliases.get(transition.kind()))
                                                    .isWriteIndex(true)));
                            }
                            return update;
                        });
            if (!request.acknowledged()) {
                throw new IllegalStateException(
                    "search alias switch was not acknowledged; inspect aliases before retrying");
            }
            transitions.forEach(
                transition ->
                    log.info(
                        "Switched search alias {} from {} to {}",
                        aliases.get(transition.kind()),
                        transition.previousIndex(),
                        transition.index()));
        } catch (IOException failure) {
            throw new UncheckedIOException(
                "Search alias switch outcome is unknown; inspect aliases before retrying",
                failure);
        }
    }

    private void write(String index, List<IndexQuery> queries) {
        if (!queries.isEmpty()) {
            template.bulkIndex(queries, IndexCoordinates.of(index));
        }
    }

    private String currentIndex(SearchIndexKind kind, String alias) {
        try {
            var targets = indices.getAlias(request -> request.name(alias)).aliases().keySet();
            if (targets.size() != 1) {
                throw new IndexRebuildRejectedException(
                    alias + " must point to exactly one index for " + kind);
            }
            return targets.iterator().next();
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static String requireAlias(String alias) {
        if (!alias.matches("[a-z0-9][a-z0-9_-]*")) {
            throw new IllegalArgumentException("invalid search index alias");
        }
        return alias;
    }
}
