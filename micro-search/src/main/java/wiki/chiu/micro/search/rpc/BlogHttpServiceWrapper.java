package wiki.chiu.micro.search.rpc;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;

@Component
public class BlogHttpServiceWrapper {

  private final BlogHttpService blogHttpService;

  public BlogHttpServiceWrapper(BlogHttpService blogHttpService) {
    this.blogHttpService = blogHttpService;
  }

  public BlogEntityRpcVo findById(Long blogId) {
    return RemoteResult.requireSuccess(() -> blogHttpService.findById(blogId));
  }
}
