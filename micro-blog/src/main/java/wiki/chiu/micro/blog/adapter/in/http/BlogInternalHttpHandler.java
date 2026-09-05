package wiki.chiu.micro.blog.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.blog.api.BlogHttpService;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.blog.application.port.in.BlogSensitiveService;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.web.ValidatedRequest;

/**
 * Internal blog HTTP handler.
 */
@Component
public class BlogInternalHttpHandler implements BlogHttpService {

    private final BlogQueryService blogQueryService;

    private final BlogSensitiveService blogSensitiveService;
    private final ValidatedRequest v;

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
        new ParameterizedTypeReference<>() {
        };

    public BlogInternalHttpHandler(
        BlogQueryService blogQueryService,
        BlogSensitiveService blogSensitiveService,
        ValidatedRequest v) {
        this.blogQueryService = blogQueryService;
        this.blogSensitiveService = blogSensitiveService;
        this.v = v;
    }

    public ServerResponse count(ServerRequest request) {
        return ok(count());
    }

    public ServerResponse findIdsAfter(ServerRequest request) {
        Long afterId = v.nonNegative(requiredParam(request, "afterId", Long::valueOf), "afterId");
        Integer limit =
            v.range(requiredParam(request, "limit", Integer::valueOf), 1, 1000, "limit");
        return ok(findIdsAfter(afterId, limit));
    }

    public ServerResponse findSensitiveByBlogId(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        return ok(findSensitiveByBlogId(blogId));
    }

    public ServerResponse findById(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        return ok(findById(blogId));
    }

    public ServerResponse findAllById(ServerRequest request) throws Exception {
        return ok(findAllById(v.positiveElements(request.body(LONG_LIST), "ids")));
    }

    public ServerResponse findPage(ServerRequest request) {
        return ok(
            findPage(
                v.positive(requiredParam(request, "pageNo", Integer::valueOf), "pageNo"),
                v.positive(requiredParam(request, "pageSize", Integer::valueOf), "pageSize")));
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
    public Result<BlogEntityRpcVo> findById(Long blogId) {
        return Result.success(() -> blogQueryService.findById(blogId));
    }

    @Override
    public Result<List<BlogEntityRpcVo>> findAllById(List<Long> ids) {
        return Result.success(() -> blogQueryService.findAllById(ids));
    }

    @Override
    public Result<Long> count() {
        return Result.success(blogQueryService::count);
    }

    @Override
    public Result<Void> setReadCount(Long blogId) {
        return Result.success(() -> blogQueryService.incrementViews(blogId));
    }

    @Override
    public Result<PageAdapter<BlogEntityRpcVo>> findPage(Integer pageNo, Integer pageSize) {
        return Result.success(() -> blogQueryService.findPage(pageNo, pageSize));
    }

    @Override
    public Result<BlogSensitiveContentRpcVo> findSensitiveByBlogId(Long blogId) {
        return Result.success(() -> blogSensitiveService.findByBlogId(blogId));
    }
}
