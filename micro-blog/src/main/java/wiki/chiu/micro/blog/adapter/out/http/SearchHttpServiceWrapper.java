package wiki.chiu.micro.blog.adapter.out.http;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.search.api.SearchHttpService;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;

@Component
public class SearchHttpServiceWrapper implements BlogSearchGateway {

  private final SearchHttpService searchHttpService;

  public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
    this.searchHttpService = searchHttpService;
  }

  @Override
  public BlogSearchRpcVo searchBlogs(BlogSysSearchReq req) {
    return RemoteResult.requireSuccess(() -> searchHttpService.searchBlogs(req));
  }

  @Override
  public long countBlogs(BlogSysCountSearchReq req) {
    return RemoteResult.requireSuccess(() -> searchHttpService.countBlogs(req));
  }
}
