package org.chiu.micro.search.mq.handler;

import org.chiu.micro.search.dto.BlogEntityDto;
import org.chiu.micro.search.rpc.wrapper.BlogHttpServiceWrapper;
import org.chiu.micro.search.constant.BlogOperateEnum;
import org.chiu.micro.search.document.BlogDocument;

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
    protected void elasticSearchProcess(BlogEntityDto blog) {
        var blogDocument = BlogDocument.builder()
                .id(blog.getId())
                .userId(blog.getUserId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .content(blog.getContent())
                .status(blog.getStatus()).link(blog.getLink())
                .created(ZonedDateTime.of(blog.getCreated(), ZoneId.of("Asia/Shanghai")))
                .updated(ZonedDateTime.of(blog.getUpdated(), ZoneId.of("Asia/Shanghai")))
                .build();

        elasticsearchTemplate.save(blogDocument);
    }

}
