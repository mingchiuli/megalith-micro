package wiki.chiu.micro.search.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch._types.ScriptLanguage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;

public final class ElasticsearchBlogIndexWriter implements BlogIndexWriter {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final String indexName;
    private final String adminIndexName;
    private final String revisionIndexName;
    private final String contentScript;
    private final String adminScript;
    private final String readCountScript;

    public ElasticsearchBlogIndexWriter(
        ElasticsearchTemplate elasticsearchTemplate,
        String indexName,
        String adminIndexName,
        String revisionIndexName) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.indexName = indexName;
        this.adminIndexName = adminIndexName;
        this.revisionIndexName = revisionIndexName;
        this.contentScript = script("blog-content-update.painless");
        this.adminScript = script("blog-admin-update.painless");
        this.readCountScript = script("blog-read-count-update.painless");
    }

    @Override
    public void updateReadCounts(List<BlogReadCount> counts) {
        if (counts.isEmpty()) {
            return;
        }
        List<UpdateQuery> queries =
            counts.stream()
                .map(
                    count ->
                        scriptedUpdate(
                            count.blogId(),
                            readCountScript,
                            Map.of("readCount", count.readCount())))
                .toList();
        elasticsearchTemplate.bulkUpdate(queries, IndexCoordinates.of(indexName));
    }

    @Override
    public void write(BlogIndexChange change) {
        BlogDocument document = BlogDocumentMapper.publicDocument(change);
        Document source = elasticsearchTemplate.getElasticsearchConverter().mapObject(document);
        elasticsearchTemplate.update(
            scriptedUpdate(document.getId(), contentScript, Map.of("blog", source)),
            IndexCoordinates.of(indexName));
    }

    @Override
    public void writeAdmin(BlogAdminIndexChange message) {
        writeRevisionIfPresent(message.revision());
        if (message.publishedRevision() != null
            && !Objects.equals(
                message.publishedRevision().id(),
                message.revision() == null ? null : message.revision().id())) {
            writeRevisionIfPresent(message.publishedRevision());
        }
        AdminBlogDocument current =
            message.publishedRevision() == null ? loadAdminDocument(message.blog().id()) : null;
        boolean force = "RECOVER".equals(message.operation());
        if (!force
            && current != null
            && current.getAdminRevision() != null
            && message.blog().adminRevision() < current.getAdminRevision()) {
            return;
        }
        applyAdminDocument(message, current);
    }

    void applyAdminDocument(
        BlogAdminIndexChange message, AdminBlogDocument current) {
        boolean force = "RECOVER".equals(message.operation());
        AdminBlogDocument admin =
            current == null
                ? BlogDocumentMapper.adminDocument(message)
                : applyToExisting(current, message);
        Document source = elasticsearchTemplate.getElasticsearchConverter().mapObject(admin);
        elasticsearchTemplate.update(
            scriptedUpdate(
                message.blog().id(),
                adminScript,
                Map.of("admin", source, "force", force)),
            IndexCoordinates.of(adminIndexName));
    }

    private AdminBlogDocument applyToExisting(
        AdminBlogDocument current, BlogAdminIndexChange message) {
        current.apply(BlogDocumentMapper.adminProjection(current, message));
        return current;
    }

    private AdminBlogDocument loadAdminDocument(long blogId) {
        return elasticsearchTemplate.get(
            Long.toString(blogId),
            AdminBlogDocument.class,
            IndexCoordinates.of(adminIndexName));
    }

    private void writeRevisionIfPresent(
        wiki.chiu.micro.common.model.BlogRevisionSnapshot revision) {
        if (revision == null) {
            return;
        }
        elasticsearchTemplate.save(
            BlogDocumentMapper.revisionDocument(revision),
            IndexCoordinates.of(revisionIndexName));
    }

    private static UpdateQuery scriptedUpdate(
        long id, String script, Map<String, Object> params) {
        return UpdateQuery.builder(Long.toString(id))
            .withScript(script)
            .withLang(ScriptLanguage.Painless.jsonValue())
            .withParams(params)
            .withScriptedUpsert(true)
            .withUpsert(Document.create())
            .withRetryOnConflict(5)
            .build();
    }

    private static String script(String name) {
        try {
            return new ClassPathResource("script/" + name)
                .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot load search update script " + name, failure);
        }
    }
}
