package wiki.chiu.micro.blog.handler;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.blog.service.BlogSensitiveService;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;

import java.time.LocalDateTime;
import java.util.List;
import wiki.chiu.micro.common.web.ValidatedRequest;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

/**
 * Internal blog HTTP handler.
 */
@Component
public class BlogInternalHttpHandler implements BlogHttpService {

    private final BlogService blogService;

    private final BlogSensitiveService blogSensitiveService;
    private final ValidatedRequest v;

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    public BlogInternalHttpHandler(BlogService blogService, BlogSensitiveService blogSensitiveService,
                                   ValidatedRequest v) {
        this.blogService = blogService;
        this.blogSensitiveService = blogSensitiveService;
        this.v = v;
    }

    public ServerResponse count(ServerRequest request) {
        return ok(count());
    }

    public ServerResponse countByCreatedGreaterThanEqual(ServerRequest request) {
        LocalDateTime created = requiredParam(request, "created", LocalDateTime::parse);
        return ok(countByCreatedGreaterThanEqual(created));
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
        return ok(findPage(v.positive(requiredParam(request, "pageNo", Integer::valueOf), "pageNo"),
                v.positive(requiredParam(request, "pageSize", Integer::valueOf), "pageSize")));
    }

    public ServerResponse setReadCount(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        return ok(setReadCount(blogId));
    }

    @Override
    public Result<BlogEntityRpcVo> findById(Long blogId) {
        return Result.success(() -> blogService.findById(blogId));
    }

    @Override
    public Result<List<BlogEntityRpcVo>> findAllById(List<Long> ids) {
        return Result.success(() -> blogService.findAllById(ids));
    }

    @Override
    public Result<Long> count() {
        return Result.success(blogService::count);
    }

    @Override
    public Result<Void> setReadCount(Long blogId) {
        return Result.success(() -> blogService.setReadCount(blogId));
    }

    @Override
    public Result<PageAdapter<BlogEntityRpcVo>> findPage(Integer pageNo, Integer pageSize) {
        return Result.success(() -> blogService.findPage(pageNo, pageSize));
    }

    @Override
    public Result<Long> countByCreatedGreaterThanEqual(LocalDateTime created) {
        return Result.success(() -> blogService.countByCreatedGreaterThanEqual(created));
    }

    @Override
    public Result<BlogSensitiveContentRpcVo> findSensitiveByBlogId(Long blogId) {
        return Result.success(() -> blogSensitiveService.findByBlogId(blogId));
    }
}
