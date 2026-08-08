package wiki.chiu.micro.blog.rpc;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.vo.BlogSearchRpcVo;

@Component
public class SearchHttpServiceWrapper {

  private final SearchHttpService searchHttpService;

  public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
    this.searchHttpService = searchHttpService;
  }

  public BlogSearchRpcVo searchBlogs(BlogSysSearchReq req) {
    return RemoteResult.requireSuccess(() -> searchHttpService.searchBlogs(req));
  }

  public Long countBlogs(BlogSysCountSearchReq req) {
    return RemoteResult.requireSuccess(() -> searchHttpService.countBlogs(req));
  }
}
