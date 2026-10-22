package wiki.chiu.micro.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.search.adapter.out.elasticsearch.AdminBlogDocument;
import wiki.chiu.micro.search.adapter.out.elasticsearch.AdminRevisionDocument;

@Component
public class AdminSearchIndexInitializer {

    private final ElasticsearchClient client;
    private final ElasticsearchTemplate template;
    private final String adminAlias;
    private final String revisionAlias;

    public AdminSearchIndexInitializer(
        ElasticsearchClient client,
        ElasticsearchTemplate template,
        @Value("${megalith.search.index.admin-alias:blog_admin_search}") String adminAlias,
        @Value("${megalith.search.index.revision-alias:blog_revision_search}")
            String revisionAlias) {
        this.client = client;
        this.template = template;
        this.adminAlias = adminAlias;
        this.revisionAlias = revisionAlias;
    }

    public void ensureAliases() throws IOException {
        ensureAlias(adminAlias, AdminBlogDocument.class);
        ensureAlias(revisionAlias, AdminRevisionDocument.class);
    }

    private void ensureAlias(String alias, Class<?> documentType) throws IOException {
        if (client.indices().existsAlias(request -> request.name(alias)).value()) {
            return;
        }
        if (client.indices().exists(request -> request.index(alias)).value()) {
            throw new IllegalStateException("admin search alias is occupied by an index: " + alias);
        }
        String physical = alias + "-v1-" + UUID.randomUUID();
        var operations = template.indexOps(IndexCoordinates.of(physical));
        if (!operations.create(Map.of(), operations.createMapping(documentType))) {
            throw new IllegalStateException("admin search index creation failed: " + physical);
        }
        var result =
            client.indices()
                .updateAliases(
                    request ->
                        request.actions(
                            action ->
                                action.add(
                                    add ->
                                        add.index(physical)
                                            .alias(alias)
                                            .isWriteIndex(true))));
        if (!result.acknowledged()) {
            throw new IllegalStateException("admin search alias creation failed: " + alias);
        }
    }
}
