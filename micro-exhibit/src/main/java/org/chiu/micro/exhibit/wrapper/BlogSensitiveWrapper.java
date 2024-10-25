package org.chiu.micro.exhibit.wrapper;

import org.chiu.micro.cache.annotation.Cache;
import org.chiu.micro.common.dto.BlogSensitiveContentRpcDto;
import org.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import org.springframework.stereotype.Component;

import static org.chiu.micro.common.lang.Const.HOT_BLOG;

@Component
public class BlogSensitiveWrapper {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    public BlogSensitiveWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
    }

    @Cache(prefix = HOT_BLOG)
    public BlogSensitiveContentRpcDto findSensitiveByBlogId(Long blogId) {
        return blogHttpServiceWrapper.findSensitiveByBlogId(blogId);
    }
}
