package wiki.chiu.micro.search.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch._types.ScriptLanguage;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.VersionConflictException;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.SearchPage;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

public final class ElasticsearchBlogAdapter implements BlogSearchIndex, BlogIndexWriter {

  private static final Logger log = LoggerFactory.getLogger(ElasticsearchBlogAdapter.class);
  private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

  private final ElasticsearchTemplate elasticsearchTemplate;
  private final int publicPageSize;

  public ElasticsearchBlogAdapter(
      ElasticsearchTemplate elasticsearchTemplate, int publicPageSize) {
    this.elasticsearchTemplate = elasticsearchTemplate;
    this.publicPageSize = publicPageSize;
  }

  @Override
  public SearchPage<BlogSearchHit> searchPublic(PublicBlogSearchQuery query) {
    NativeQuery searchQuery =
        PublicSearchQueryConvertor.searchConvert(
            query.keywords(), query.page(), publicPageSize, query.allInfo());
    SearchHits<@NonNull BlogDocument> hits =
        elasticsearchTemplate.search(searchQuery, BlogDocument.class);
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
        elasticsearchTemplate.search(searchQuery, BlogDocument.class);
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
    return elasticsearchTemplate.count(countQuery, BlogDocument.class);
  }

  @Override
  public void incrementViews(long blogId) {
    UpdateQuery updateQuery =
        UpdateQuery.builder(Long.toString(blogId))
            .withScript("ctx._source.readCount += 1;")
            .withLang(ScriptLanguage.Painless.jsonValue())
            .build();
    elasticsearchTemplate.update(updateQuery, IndexCoordinates.of(IndexConst.indexName));
  }

  @Override
  public void write(BlogIndexChange change) {
    BlogDocument document = toDocument(change);
    IndexQuery query =
        IndexQuery.builder()
            .withId(document.getId().toString())
            .withObject(document)
            .withVersion(change.revision())
            .build();
    try {
      elasticsearchTemplate.index(query, IndexCoordinates.of(IndexConst.indexName));
    } catch (VersionConflictException staleMessage) {
      log.debug("Ignored stale blog index revision {} for {}", change.revision(), document.getId());
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

  private static BlogDocument toDocument(BlogIndexChange change) {
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
