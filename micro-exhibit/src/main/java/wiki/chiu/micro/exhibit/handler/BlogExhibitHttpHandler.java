package wiki.chiu.micro.exhibit.handler;

import static wiki.chiu.micro.common.auth.web.AuthWeb.authPrincipal;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.cache.annotation.Checker;
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

  public BlogExhibitHttpHandler(BlogService blogService, ValidatedRequest v) {
    this.blogService = blogService;
    this.v = v;
  }

  @Checker(handler = DetailHandler.class)
  public ServerResponse getBlogDetail(ServerRequest request) {
    Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
    AuthPrincipal authInfo = authPrincipal(request);
    return ok(
        Result.success(
            () ->
                blogService.getBlogDetail(authInfo.dataPermissions(), blogId, authInfo.userId())));
  }

  @Checker(handler = ListPageHandler.class)
  public ServerResponse getPage(ServerRequest request) {
    Integer currentPage =
        v.positive(pathVariable(request, "currentPage", Integer::valueOf), "currentPage");
    return ok(Result.success(() -> blogService.findPage(currentPage)));
  }

  @Checker(handler = DetailHandler.class)
  public ServerResponse getLockedBlog(ServerRequest request) throws Exception {
    Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
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
