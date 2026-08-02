package wiki.chiu.micro.exhibit.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.cache.annotation.Checker;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.exhibit.checker.handler.DetailHandler;
import wiki.chiu.micro.exhibit.checker.handler.ListPageHandler;
import wiki.chiu.micro.exhibit.service.BlogService;
import static wiki.chiu.micro.common.web.FunctionalWeb.authInfo;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.positive;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

@Component
public class BlogExhibitHttpHandler {

    private final BlogService blogService;
    private final AuthHttpService authHttpService;

    public BlogExhibitHttpHandler(BlogService blogService, AuthHttpService authHttpService) {
        this.blogService = blogService;
        this.authHttpService = authHttpService;
    }

    @Checker(handler = DetailHandler.class)
    public ServerResponse getBlogDetail(ServerRequest request) {
        Long blogId = positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.getBlogDetail(
                authInfo.roles(), blogId, authInfo.userId())));
    }

    @Checker(handler = ListPageHandler.class)
    public ServerResponse getPage(ServerRequest request) {
        Integer currentPage = positive(pathVariable(request, "currentPage", Integer::valueOf), "currentPage");
        return ok(Result.success(() -> blogService.findPage(currentPage)));
    }

    @Checker(handler = DetailHandler.class)
    public ServerResponse getLockedBlog(ServerRequest request) {
        Long blogId = positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        String token = requiredParam(request, "readToken");
        return ok(Result.success(blogService.getLockedBlog(blogId, token)));
    }

    @Checker(handler = DetailHandler.class)
    public ServerResponse checkReadToken(ServerRequest request) {
        Long blogId = positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        String token = requiredParam(request, "readToken");
        return ok(Result.success(() -> blogService.checkToken(blogId, token)));
    }

    public ServerResponse getVisitStatistics(ServerRequest request) {
        return ok(Result.success(blogService::getVisitStatistics));
    }

    public ServerResponse getScoreBlogs(ServerRequest request) {
        return ok(Result.success(blogService::getScoreBlogs));
    }
}
