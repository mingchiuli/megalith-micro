package wiki.chiu.micro.search.mq.handler;

import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.search.document.BlogDocument;
import wiki.chiu.micro.search.rpc.BlogHttpServiceWrapper;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * @author mingchiuli
 * @create 2022-12-03 4:50 pm
 */
@Component
public final class UpdateBlogIndexHandler extends BlogIndexSupport {

    private final ElasticsearchTemplate elasticsearchTemplate;

    public UpdateBlogIndexHandler(BlogHttpServiceWrapper blogHttpServiceWrapper,
                                  ElasticsearchTemplate elasticsearchTemplate) {
        super(blogHttpServiceWrapper);
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.UPDATE.equals(blogOperateEnum);
    }

    @Override
    protected void elasticSearchProcess(BlogEntityRpcVo blog) {
        var blogDocument = BlogDocument.builder()
                .id(blog.id())
                .userId(blog.userId())
                .title(blog.title())
                .readCount(blog.readCount())
                .description(blog.description()).content(blog.content())
                .status(blog.status())
                .created(ZonedDateTime.of(blog.created(), ZoneId.of("Asia/Shanghai")))
                .updated(ZonedDateTime.of(blog.updated(), ZoneId.of("Asia/Shanghai")))
                .build();

        elasticsearchTemplate.update(blogDocument);
    }
}
