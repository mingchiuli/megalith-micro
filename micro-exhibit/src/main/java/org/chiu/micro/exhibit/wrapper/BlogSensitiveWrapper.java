package org.chiu.micro.exhibit.wrapper;

import static org.chiu.micro.exhibit.lang.Const.*;

import org.chiu.micro.exhibit.cache.config.Cache;
import org.chiu.micro.exhibit.dto.BlogSensitiveContentDto;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlogSensitiveWrapper {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    @Cache(prefix = HOT_BLOG)
    public BlogSensitiveContentDto findSensitiveByBlogId(Long blogId) {
        return blogHttpServiceWrapper.findSensitiveByBlogId(blogId);
    }
}
