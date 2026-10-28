package wiki.chiu.micro.search.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import wiki.chiu.micro.search.application.port.out.SearchIndexSchema;

public final class ElasticsearchSearchIndexSchemaAdapter implements SearchIndexSchema {

    private static final Logger log =
        LoggerFactory.getLogger(ElasticsearchSearchIndexSchemaAdapter.class);

    private final ElasticsearchClient client;
    private final ElasticsearchTemplate template;
    private final String publicAlias;
    private final String adminAlias;
    private final String revisionAlias;

    public ElasticsearchSearchIndexSchemaAdapter(
        ElasticsearchClient client,
        ElasticsearchTemplate template,
        String publicAlias,
        String adminAlias,
        String revisionAlias) {
        this.client = client;
        this.template = template;
        this.publicAlias = requireAlias(publicAlias);
        this.adminAlias = requireAlias(adminAlias);
        this.revisionAlias = requireAlias(revisionAlias);
    }

    @Override
    public void ensureAliases() {
        ensureAlias(publicAlias, BlogDocument.class);
        ensureAlias(adminAlias, AdminBlogDocument.class);
        ensureAlias(revisionAlias, AdminRevisionDocument.class);
    }

    private void ensureAlias(String alias, Class<?> documentType) {
        try {
            if (client.indices().existsAlias(request -> request.name(alias)).value()) {
                int targets =
                    client.indices()
                        .getAlias(request -> request.name(alias))
                        .aliases()
                        .size();
                if (targets != 1) {
                    throw new IllegalStateException(
                        "search alias must point to exactly one index: " + alias);
                }
                return;
            }
            if (client.indices().exists(request -> request.index(alias)).value()) {
                throw new IllegalStateException(
                    "search alias is occupied by a concrete index: " + alias);
            }
            String target = alias + "-v1-" + UUID.randomUUID();
            var operations = template.indexOps(IndexCoordinates.of(target));
            if (!operations.create(Map.of(), operations.createMapping(documentType))) {
                throw new IllegalStateException("search index creation failed: " + target);
            }
            var result =
                client.indices()
                    .updateAliases(
                        request ->
                            request.actions(
                                action ->
                                    action.add(
                                        add ->
                                            add.index(target)
                                                .alias(alias)
                                                .isWriteIndex(true))));
            if (!result.acknowledged()) {
                throw new IllegalStateException(
                    "search alias creation was not acknowledged: " + alias);
            }
            log.info("Created search index {} with alias {}", target, alias);
        } catch (IOException failure) {
            throw new UncheckedIOException("cannot initialize search alias " + alias, failure);
        }
    }

    private static String requireAlias(String alias) {
        if (alias == null || !alias.matches("[a-z0-9][a-z0-9_-]*")) {
            throw new IllegalArgumentException("invalid search index alias");
        }
        return alias;
    }
}
