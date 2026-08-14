package wiki.chiu.micro.search.mq.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.VersionConflictException;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.search.document.BlogDocument;
import wiki.chiu.micro.search.lang.IndexConst;

public abstract sealed class BlogIndexSupport
    permits CreateBlogIndexHandler, RemoveBlogIndexHandler, UpdateBlogIndexHandler {

  private static final Logger log = LoggerFactory.getLogger(BlogIndexSupport.class);

  public abstract boolean supports(BlogOperateEnum blogOperateEnum);

  protected abstract void elasticSearchProcess(BlogChangedMessage message);

  public void process(BlogChangedMessage message) {
    elasticSearchProcess(message);
  }

  protected void indexVersioned(
      ElasticsearchTemplate elasticsearchTemplate, BlogDocument document, long revision) {
    IndexQuery query =
        IndexQuery.builder()
            .withId(document.getId().toString())
            .withObject(document)
            .withVersion(revision)
            .build();
    try {
      elasticsearchTemplate.index(query, IndexCoordinates.of(IndexConst.indexName));
    } catch (VersionConflictException staleMessage) {
      log.debug("Ignored stale blog index revision {} for {}", revision, document.getId());
    }
  }
}
