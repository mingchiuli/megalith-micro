package wiki.chiu.micro.blog.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.blog.api.BlogHttpService;
import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.common.web.ValidatedRequest;

/**
 * Internal blog HTTP handler.
 */
@Component
public class BlogInternalHttpHandler implements BlogHttpService {

    private final BlogQueryService blogQueryService;

    private final ValidatedRequest v;

    public BlogInternalHttpHandler(BlogQueryService blogQueryService, ValidatedRequest v) {
        this.blogQueryService = blogQueryService;
        this.v = v;
    }

    public ServerResponse findIdsAfter(ServerRequest request) {
        Long afterId = v.nonNegative(requiredParam(request, "afterId", Long::valueOf), "afterId");
        Integer limit =
            v.range(requiredParam(request, "limit", Integer::valueOf), 1, 1000, "limit");
        return ok(findIdsAfter(afterId, limit));
    }

    public ServerResponse setReadCount(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        return ok(setReadCount(blogId));
    }

    @Override
    public Result<List<Long>> findIdsAfter(Long afterId, Integer limit) {
        return Result.success(() -> blogQueryService.findIdsAfter(afterId, limit));
    }

    @Override
    public Result<Void> setReadCount(Long blogId) {
        return Result.success(() -> blogQueryService.incrementViews(blogId));
    }

}
