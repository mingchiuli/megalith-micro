package wiki.chiu.micro.search.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.search.api.SearchHttpService;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;
import wiki.chiu.micro.search.convertor.SearchRequestConvertor;
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
    return ok(searchBlogs(SearchRequestConvertor.toBlogSysSearchReq(request)));
  }

  public ServerResponse countBlogs(ServerRequest request) throws Exception {
    return ok(countBlogs(SearchRequestConvertor.toBlogSysCountSearchReq(request)));
  }

  public ServerResponse addReadCount(ServerRequest request) {
    Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
    return ok(addReadCount(blogId));
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
  public Result<Void> addReadCount(Long blogId) {
    return Result.success(() -> blogSearchService.addReadCount(blogId));
  }
}
