package org.chiu.micro.search.mq.handler;

import org.chiu.micro.search.constant.BlogOperateEnum;
import org.chiu.micro.search.document.BlogDocument;
import org.chiu.micro.search.dto.BlogEntityDto;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;
import org.chiu.micro.search.rpc.wrapper.BlogHttpServiceWrapper;

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
                .id(blog.getId())
                .userId(blog.getUserId())
                .title(blog.getTitle())
                .description(blog.getDescription()).content(blog.getContent())
                .status(blog.getStatus())
                .link(blog.getLink())
                .created(ZonedDateTime.of(blog.getCreated(), ZoneId.of("Asia/Shanghai")))
                .updated(ZonedDateTime.of(blog.getUpdated(), ZoneId.of("Asia/Shanghai")))
                .build();

        elasticsearchTemplate.update(blogDocument);
    }
}
