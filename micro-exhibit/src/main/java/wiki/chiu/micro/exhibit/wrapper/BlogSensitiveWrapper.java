package wiki.chiu.micro.exhibit.wrapper;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.exhibit.cache.BlogCacheDescriptors;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;

@Component
public class BlogSensitiveWrapper {

  private final BlogHttpServiceWrapper blogHttpServiceWrapper;

  public BlogSensitiveWrapper(BlogHttpServiceWrapper blogHttpServiceWrapper) {
    this.blogHttpServiceWrapper = blogHttpServiceWrapper;
  }

  @Cache(
      namespace = BlogCacheDescriptors.SENSITIVE_NAMESPACE,
      version = BlogCacheDescriptors.VERSION)
  public BlogSensitiveContentRpcVo findSensitiveByBlogId(Long blogId) {
    return blogHttpServiceWrapper.findSensitiveByBlogId(blogId);
  }
}
