package org.chiu.micro.exhibit.wrapper;

import org.chiu.micro.exhibit.cache.config.Cache;
import org.chiu.micro.exhibit.dto.BlogSensitiveContentDto;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.springframework.stereotype.Component;

import static org.chiu.micro.exhibit.lang.Const.HOT_BLOG;

@Component
public class BlogSensitiveWrapper {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    public BlogSensitiveWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
    }

    @Cache(prefix = HOT_BLOG)
    public BlogSensitiveContentDto findSensitiveByBlogId(Long blogId) {
        return blogHttpServiceWrapper.findSensitiveByBlogId(blogId);
    }
}
