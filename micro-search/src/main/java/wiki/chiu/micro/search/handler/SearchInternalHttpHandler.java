package wiki.chiu.micro.search.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.vo.BlogSearchRpcVo;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.search.converter.SearchRequestConverter;
import wiki.chiu.micro.search.service.BlogSearchService;

@Component
public class SearchInternalHttpHandler implements SearchHttpService {

  private final BlogSearchService blogSearchService;
  private final ValidatedRequest v;

  public SearchInternalHttpHandler(BlogSearchService blogSearchService, ValidatedRequest v) {
    this.blogSearchService = blogSearchService;
    this.v = v;
  }

  public ServerResponse searchBlogs(ServerRequest request) throws Exception {
    return ok(searchBlogs(SearchRequestConverter.toBlogSysSearchReq(request)));
  }

  public ServerResponse countBlogs(ServerRequest request) throws Exception {
    return ok(countBlogs(SearchRequestConverter.toBlogSysCountSearchReq(request)));
  }

  public ServerResponse addReadCount(ServerRequest request) {
    Long id = v.positive(requiredParam(request, "id", Long::valueOf), "id");
    return ok(addReadCount(id));
  }

  @Override
  public Result<BlogSearchRpcVo> searchBlogs(BlogSysSearchReq req) {
    return Result.success(() -> blogSearchService.searchBlogs(req));
  }

  @Override
  public Result<Long> countBlogs(BlogSysCountSearchReq req) {
    return Result.success(() -> blogSearchService.searchCount(req));
  }

  @Override
  public Result<Void> addReadCount(Long id) {
    return Result.success(() -> blogSearchService.addReadCount(id));
  }
}
