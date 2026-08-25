package wiki.chiu.micro.exhibit.handler;

import static wiki.chiu.micro.common.auth.web.AuthWeb.authPrincipal;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.exhibit.checker.handler.DetailHandler;
import wiki.chiu.micro.exhibit.checker.handler.ListPageHandler;
import wiki.chiu.micro.exhibit.req.ReadTokenReq;
import wiki.chiu.micro.exhibit.service.BlogService;

@Component
public class BlogExhibitHttpHandler {

  private final BlogService blogService;
  private final ValidatedRequest v;
  private final DetailHandler detailHandler;
  private final ListPageHandler listPageHandler;

  public BlogExhibitHttpHandler(
      BlogService blogService,
      ValidatedRequest v,
      DetailHandler detailHandler,
      ListPageHandler listPageHandler) {
    this.blogService = blogService;
    this.v = v;
    this.detailHandler = detailHandler;
    this.listPageHandler = listPageHandler;
  }

  public ServerResponse getBlogDetail(ServerRequest request) {
    Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
    detailHandler.check(blogId);
    AuthPrincipal authInfo = authPrincipal(request);
    return ok(
        Result.success(
            () ->
                blogService.getBlogDetail(authInfo.dataPermissions(), blogId, authInfo.userId())));
  }

  public ServerResponse getPage(ServerRequest request) {
    Integer currentPage =
        v.positive(pathVariable(request, "currentPage", Integer::valueOf), "currentPage");
    listPageHandler.check(currentPage);
    return ok(Result.success(() -> blogService.findPage(currentPage)));
  }

  public ServerResponse getLockedBlog(ServerRequest request) throws Exception {
    Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
    detailHandler.check(blogId);
    ReadTokenReq body = request.body(ReadTokenReq.class);
    String token = v.notBlank(body.readToken(), "readToken");
    return ok(Result.success(blogService.getLockedBlog(blogId, token)));
  }

  public ServerResponse getVisitStatistics(ServerRequest request) {
    return ok(Result.success(blogService::getVisitStatistics));
  }

  public ServerResponse getScoreBlogs(ServerRequest request) {
    return ok(Result.success(blogService::getScoreBlogs));
  }
}
