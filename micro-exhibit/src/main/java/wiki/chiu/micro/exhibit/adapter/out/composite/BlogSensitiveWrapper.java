package wiki.chiu.micro.exhibit.adapter.out.composite;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.exhibit.adapter.out.http.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.application.port.out.SensitiveContentReader;
import wiki.chiu.micro.exhibit.cache.BlogCacheDescriptors;

@Component
public class BlogSensitiveWrapper implements SensitiveContentReader {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    public BlogSensitiveWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
    }

    @Cache(
        namespace = BlogCacheDescriptors.SENSITIVE_NAMESPACE,
        version = BlogCacheDescriptors.VERSION)
    @Override
    public BlogSensitiveContentRpcVo findSensitiveByBlogId(Long blogId) {
        return blogHttpServiceWrapper.findSensitiveByBlogId(blogId);
    }
}
