package wiki.chiu.micro.search.mq.handler;

import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.search.document.BlogDocument;

/**
 * @author mingchiuli
 * @create 2022-12-03 3:55 pm
 */
@Component
public final class RemoveBlogIndexHandler extends BlogIndexSupport {
  private final ElasticsearchTemplate elasticsearchTemplate;

  public RemoveBlogIndexHandler(ElasticsearchTemplate elasticsearchTemplate) {
    this.elasticsearchTemplate = elasticsearchTemplate;
  }

  @Override
  public boolean supports(BlogOperateEnum blogOperateEnum) {
    return BlogOperateEnum.REMOVE.equals(blogOperateEnum);
  }

  @Override
  protected void elasticSearchProcess(BlogChangedMessage message) {
    BlogDocument tombstone =
        BlogDocument.builder()
            .id(message.blogSnapshot().id())
            .revision(message.revision())
            .deleted(true)
            .build();
    indexVersioned(elasticsearchTemplate, tombstone, message.revision());
  }
}
