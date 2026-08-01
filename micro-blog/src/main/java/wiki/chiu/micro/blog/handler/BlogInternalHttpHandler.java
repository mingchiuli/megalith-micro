package wiki.chiu.micro.blog.handler;

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

/**
 * Internal blog HTTP handler.
 */
@Component
public class BlogInternalHttpHandler implements BlogHttpService {

    private final BlogService blogService;

    private final BlogSensitiveService blogSensitiveService;

    public BlogInternalHttpHandler(BlogService blogService, BlogSensitiveService blogSensitiveService) {
        this.blogService = blogService;
        this.blogSensitiveService = blogSensitiveService;
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
