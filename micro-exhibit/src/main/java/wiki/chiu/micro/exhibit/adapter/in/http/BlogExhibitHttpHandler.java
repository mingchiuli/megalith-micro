package wiki.chiu.micro.exhibit.adapter.in.http;

import static wiki.chiu.micro.common.auth.web.AuthWeb.authPrincipal;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.nullableParam;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.exhibit.application.port.in.BlogService;

@Component
public class BlogExhibitHttpHandler {

    private final BlogService blogService;
    private final ValidatedRequest v;

    public BlogExhibitHttpHandler(BlogService blogService, ValidatedRequest v) {
        this.blogService = blogService;
        this.v = v;
    }

    public ServerResponse getBlogDetail(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    ExhibitResponseMapper.toVo(
                        blogService.getBlogDetail(
                            authInfo.dataPermissions(), blogId, authInfo.userId()))));
    }

    public ServerResponse getPage(ServerRequest request) {
        Integer currentPage =
            v.positive(pathVariable(request, "currentPage", Integer::valueOf), "currentPage");
        return ok(
            Result.success(() -> ExhibitResponseMapper.toVo(blogService.findPage(currentPage))));
    }

    public ServerResponse getLockedBlog(ServerRequest request) throws Exception {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        ReadTokenReq body = request.body(ReadTokenReq.class);
        String token = v.notBlank(body.readToken(), "readToken");
        return ok(
            Result.success(
                () -> ExhibitResponseMapper.toVo(blogService.getLockedBlog(blogId, token))));
    }

    public ServerResponse getVisitStatistics(ServerRequest request) {
        return ok(Result.success(() -> ExhibitResponseMapper.toVo(blogService.getVisitStatistics())));
    }

    public ServerResponse getScoreBlogs(ServerRequest request) {
        return ok(Result.success(() -> ExhibitResponseMapper.toHotReadVos(blogService.getScoreBlogs())));
    }

    public ServerResponse getRecommendations(ServerRequest request) {
        Long blogId = nullableParam(request, "blogId", Long::valueOf);
        if (blogId != null) {
            v.positive(blogId, "blogId");
        }
        Integer limit =
            v.range(requiredParam(request, "limit", Integer::valueOf), 1, 10, "limit");
        return ok(
            Result.success(
                () ->
                    ExhibitResponseMapper.toVo(
                        blogService.getRecommendations(blogId, limit))));
    }
}
