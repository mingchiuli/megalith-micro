package wiki.chiu.micro.search.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch._types.ScriptLanguage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.SearchPage;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

public final class ElasticsearchBlogAdapter implements BlogSearchIndex, BlogIndexWriter {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final int publicPageSize;
    private final String indexName;
    private final String contentScript;
    private final String readCountScript;

    public ElasticsearchBlogAdapter(
        ElasticsearchTemplate elasticsearchTemplate, int publicPageSize) {
        this(elasticsearchTemplate, publicPageSize, IndexConst.indexName);
    }

    public ElasticsearchBlogAdapter(
        ElasticsearchTemplate elasticsearchTemplate, int publicPageSize, String indexName) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.publicPageSize = publicPageSize;
        this.indexName = indexName;
        this.contentScript = script("blog-content-update.painless");
        this.readCountScript = script("blog-read-count-update.painless");
    }

    @Override
    public SearchPage<BlogSearchHit> searchPublic(PublicBlogSearchQuery query) {
        NativeQuery searchQuery =
            PublicSearchQueryConvertor.searchConvert(
                query.keywords(), query.page(), publicPageSize, query.allInfo());
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(searchQuery, BlogDocument.class, IndexCoordinates.of(indexName));
        return toPage(hits, query.page(), publicPageSize);
    }

    @Override
    public BlogSearchResult searchPrivate(PrivateBlogSearchQuery query) {
        NativeQuery searchQuery =
            PrivateSearchQueryConvertor.searchConvert(
                query.keywords(),
                query.status(),
                query.createStart(),
                query.createEnd(),
                query.userId(),
                query.allData(),
                query.page(),
                query.pageSize());
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(searchQuery, BlogDocument.class, IndexCoordinates.of(indexName));
        return new BlogSearchResult(
            hits.getTotalHits(),
            query.page(),
            query.pageSize(),
            hits.getSearchHits().stream().map(hit -> hit.getContent().getId()).toList());
    }

    @Override
    public long countPrivate(PrivateBlogSearchQuery query) {
        NativeQuery countQuery =
            PrivateSearchQueryConvertor.countConvert(
                query.keywords(),
                query.status(),
                query.createStart(),
                query.createEnd(),
                query.userId(),
                query.allData());
        return elasticsearchTemplate.count(countQuery, BlogDocument.class, IndexCoordinates.of(indexName));
    }

    @Override
    public void updateReadCounts(List<BlogReadCount> counts) {
        if (counts.isEmpty()) {
            return;
        }
        List<UpdateQuery> queries = counts.stream()
            .map(count -> scriptedUpdate(count.blogId(), readCountScript,
                Map.of("readCount", count.readCount())))
            .toList();
        elasticsearchTemplate.bulkUpdate(queries, IndexCoordinates.of(indexName));
    }

    @Override
    public void write(BlogIndexChange change) {
        BlogDocument document = toDocument(change);
        Document source = elasticsearchTemplate.getElasticsearchConverter().mapObject(document);
        elasticsearchTemplate.update(
            scriptedUpdate(document.getId(), contentScript, Map.of("blog", source)),
            IndexCoordinates.of(indexName));
    }

    private static UpdateQuery scriptedUpdate(long id, String script, Map<String, Object> params) {
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
            return new ClassPathResource("script/" + name).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot load search update script " + name, failure);
        }
    }

    private static SearchPage<BlogSearchHit> toPage(
        SearchHits<@NonNull BlogDocument> hits, int page, int pageSize) {
        long totalElements = hits.getTotalHits();
        int totalPages = (int) ((totalElements + pageSize - 1) / pageSize);
        return new SearchPage<>(
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
                            document.getCreated().toLocalDateTime(),
                            hit.getScore(),
                            hit.getHighlightFields());
                    })
                .toList(),
            totalElements,
            page,
            pageSize,
            page == 1,
            page == totalPages,
            totalElements == 0,
            totalPages);
    }

    static BlogDocument toDocument(BlogIndexChange change) {
        BlogIndexEntry blog = change.blog();
        if (change.operation() == BlogIndexChange.Operation.REMOVE) {
            return BlogDocument.builder()
                .id(blog.id())
                .revision(change.revision())
                .deleted(true)
                .build();
        }
        return BlogDocument.builder()
            .id(blog.id())
            .userId(blog.userId())
            .title(blog.title())
            .description(blog.description())
            .content(blog.content())
            .readCount(blog.readCount())
            .status(blog.status())
            .created(ZonedDateTime.of(blog.created(), ZONE_ID))
            .updated(ZonedDateTime.of(blog.updated(), ZONE_ID))
            .revision(change.revision())
            .deleted(false)
            .build();
    }
}
