package wiki.chiu.micro.search.mq.handler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.search.document.BlogDocument;

@Component
public final class CreateBlogIndexHandler extends BlogIndexSupport {

  private final ElasticsearchTemplate elasticsearchTemplate;

  public CreateBlogIndexHandler(ElasticsearchTemplate elasticsearchTemplate) {
    this.elasticsearchTemplate = elasticsearchTemplate;
  }

  @Override
  public boolean supports(BlogOperateEnum blogOperateEnum) {
    return BlogOperateEnum.CREATE.equals(blogOperateEnum);
  }

  @Override
  protected void elasticSearchProcess(BlogChangedMessage message) {
    BlogSnapshot blog = message.blogSnapshot();
    var blogDocument =
        BlogDocument.builder()
            .id(blog.id())
            .userId(blog.userId())
            .title(blog.title())
            .description(blog.description())
            .content(blog.content())
            .readCount(blog.readCount())
            .status(blog.status())
            .created(ZonedDateTime.of(blog.created(), ZoneId.of("Asia/Shanghai")))
            .updated(ZonedDateTime.of(blog.updated(), ZoneId.of("Asia/Shanghai")))
            .revision(message.revision())
            .deleted(false)
            .build();

    indexVersioned(elasticsearchTemplate, blogDocument, message.revision());
  }
}
