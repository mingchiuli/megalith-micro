package wiki.chiu.micro.search.mq.handler;

import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.search.constant.BlogOperateEnum;
import wiki.chiu.micro.search.document.BlogDocument;
import wiki.chiu.micro.search.rpc.BlogHttpServiceWrapper;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;


@Component
public final class CreateBlogIndexHandler extends BlogIndexSupport {

    private final ElasticsearchTemplate elasticsearchTemplate;

    public CreateBlogIndexHandler(BlogHttpServiceWrapper blogHttpServiceWrapper,
                                  ElasticsearchTemplate elasticsearchTemplate) {
        super(blogHttpServiceWrapper);
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.CREATE.equals(blogOperateEnum);
    }

    @Override
    protected void elasticSearchProcess(BlogEntityRpcDto blog) {
        var blogDocument = BlogDocument.builder()
                .id(blog.id())
                .userId(blog.userId())
                .title(blog.title())
                .description(blog.description())
                .content(blog.content())
                .status(blog.status())
                .link(blog.link())
                .created(ZonedDateTime.of(blog.created(), ZoneId.of("Asia/Shanghai")))
                .updated(ZonedDateTime.of(blog.updated(), ZoneId.of("Asia/Shanghai")))
                .build();

        elasticsearchTemplate.save(blogDocument);
    }

}
