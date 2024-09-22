package org.chiu.micro.search.mq.handler;

import org.chiu.micro.search.constant.BlogOperateEnum;
import org.chiu.micro.search.document.BlogDocument;
import org.chiu.micro.search.dto.BlogEntityDto;
import org.chiu.micro.search.rpc.wrapper.BlogHttpServiceWrapper;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;


/**
 * @author mingchiuli
 * @create 2022-12-03 3:55 pm
 */
@Component
public final class RemoveBlogIndexHandler extends BlogIndexSupport {
    private final ElasticsearchTemplate elasticsearchTemplate;


    public RemoveBlogIndexHandler(BlogHttpServiceWrapper blogHttpServiceWrapper,
                                  ElasticsearchTemplate elasticsearchTemplate) {
        super(blogHttpServiceWrapper);
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public boolean supports(BlogOperateEnum blogOperateEnum) {
        return BlogOperateEnum.REMOVE.equals(blogOperateEnum);
    }

    @Override
    protected void elasticSearchProcess(BlogEntityDto blog) {
        elasticsearchTemplate.delete(blog.id().toString(), BlogDocument.class);
    }
}
