package wiki.chiu.micro.search.adapter.in.http;

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
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;

@Component
public class SearchInternalHttpHandler implements SearchHttpService {

  private final SearchBlogsUseCase searchBlogs;
  private final ValidatedRequest v;

  public SearchInternalHttpHandler(SearchBlogsUseCase searchBlogs, ValidatedRequest v) {
    this.searchBlogs = searchBlogs;
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
    return Result.success(() -> toResponse(searchBlogs.searchPrivate(toQuery(req))));
  }

  @Override
  public Result<Long> countBlogs(BlogSysCountSearchReq req) {
    return Result.success(() -> searchBlogs.countPrivate(toQuery(req)));
  }

  @Override
  public Result<Void> addReadCount(Long blogId) {
    return Result.success(() -> searchBlogs.incrementViews(blogId));
  }

  private static PrivateBlogSearchQuery toQuery(BlogSysSearchReq req) {
    return new PrivateBlogSearchQuery(
        req.page(),
        req.pageSize(),
        req.keywords(),
        req.status(),
        req.createStart(),
        req.createEnd(),
        req.userId(),
        req.allData());
  }

  private static PrivateBlogSearchQuery toQuery(BlogSysCountSearchReq req) {
    return new PrivateBlogSearchQuery(
        1,
        1,
        req.keywords(),
        req.status(),
        req.createStart(),
        req.createEnd(),
        req.userId(),
        req.allData());
  }

  private static BlogSearchRpcVo toResponse(BlogSearchResult result) {
    return BlogSearchRpcVo.builder()
        .ids(result.ids())
        .currentPage(result.page())
        .size(result.pageSize())
        .total(result.total())
        .build();
  }
}
