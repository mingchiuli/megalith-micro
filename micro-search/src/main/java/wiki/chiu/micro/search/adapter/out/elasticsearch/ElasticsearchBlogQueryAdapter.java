package wiki.chiu.micro.search.adapter.out.elasticsearch;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;

public final class ElasticsearchBlogQueryAdapter implements BlogSearchIndex {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final int publicPageSize;
    private final String indexName;
    private final String adminIndexName;
    private final String revisionIndexName;

    public ElasticsearchBlogQueryAdapter(
        ElasticsearchTemplate elasticsearchTemplate,
        int publicPageSize,
        String indexName,
        String adminIndexName,
        String revisionIndexName) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.publicPageSize = publicPageSize;
        this.indexName = indexName;
        this.adminIndexName = adminIndexName;
        this.revisionIndexName = revisionIndexName;
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
    public Page<PublicBlogRead> recommendationPage(int page, int pageSize) {
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(
                BlogSearchQueryFactory.recommendation(page, pageSize),
                BlogDocument.class,
                IndexCoordinates.of(indexName));
        return toPublicPage(hits, page, pageSize);
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

    private static Page<PublicBlogRead> toPublicPage(
        SearchHits<@NonNull BlogDocument> hits, int page, int pageSize) {
        int totalPages = pageCount(hits.getTotalHits(), pageSize);
        return new Page<>(
            hits.getSearchHits().stream()
                .map(hit -> BlogDocumentMapper.publicRead(hit.getContent()))
                .toList(),
            hits.getTotalHits(),
            page,
            pageSize,
            page == 1,
            page >= totalPages,
            hits.getTotalHits() == 0,
            totalPages);
    }

    private static int pageCount(long total, int pageSize) {
        return (int) ((total + pageSize - 1) / pageSize);
    }
}
