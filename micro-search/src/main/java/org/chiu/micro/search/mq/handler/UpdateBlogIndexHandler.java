package org.chiu.micro.search.mq.handler;

import org.chiu.micro.search.constant.BlogOperateEnum;
import org.chiu.micro.search.document.BlogDocument;
import org.chiu.micro.search.dto.BlogEntityDto;
import org.chiu.micro.search.rpc.wrapper.BlogHttpServiceWrapper;
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
    protected void elasticSearchProcess(BlogEntityDto blog) {
        var blogDocument = BlogDocument.builder()
                .id(blog.id())
                .userId(blog.userId())
                .title(blog.title())
                .description(blog.description()).content(blog.content())
                .status(blog.status())
                .link(blog.link())
                .created(ZonedDateTime.of(blog.created(), ZoneId.of("Asia/Shanghai")))
                .updated(ZonedDateTime.of(blog.updated(), ZoneId.of("Asia/Shanghai")))
                .build();

        elasticsearchTemplate.update(blogDocument);
    }
}
