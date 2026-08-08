package wiki.chiu.micro.exhibit.wrapper;

import static wiki.chiu.micro.common.lang.Const.HOT_BLOG;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;

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
