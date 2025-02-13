package wiki.chiu.micro.exhibit.wrapper;

import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import org.springframework.stereotype.Component;

import static wiki.chiu.micro.common.lang.Const.HOT_BLOG;

@Component
public class BlogSensitiveWrapper {

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    public BlogSensitiveWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper) {
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
    }

    @Cache(prefix = HOT_BLOG)
    public BlogSensitiveContentRpcVo findSensitiveByBlogId(Long blogId) {
        return blogHttpServiceWrapper.findSensitiveByBlogId(blogId);
    }
}
