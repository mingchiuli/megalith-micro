package wiki.chiu.micro.exhibit.rpc;


import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BlogHttpServiceWrapper
 */
@Component
public class BlogHttpServiceWrapper {

    private final BlogHttpService blogHttpService;

    public BlogHttpServiceWrapper(BlogHttpService blogHttpService) {
        this.blogHttpService = blogHttpService;
    }

    public BlogEntityRpcVo findById(Long blogId) {
        return Result.handleResult(() -> blogHttpService.findById(blogId));
    }

    public List<BlogEntityRpcVo> findAllById(List<Long> ids) {
        return Result.handleResult(() -> blogHttpService.findAllById(ids));

    }

    public Long count() {
        return Result.handleResult(blogHttpService::count);
    }

    public void setReadCount(Long id) {
        blogHttpService.setReadCount(id);
    }

    public Integer findStatusById(Long blogId) {
        return Result.handleResult(() -> blogHttpService.findStatusById(blogId));

    }

    public PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize) {
        return Result.handleResult(() -> blogHttpService.findPage(pageNo, pageSize));

    }

    public long countByCreatedGreaterThanEqual(LocalDateTime created) {
        return Result.handleResult(() -> blogHttpService.countByCreatedGreaterThanEqual(created));

    }

    public BlogSensitiveContentRpcVo findSensitiveByBlogId(Long blogId) {
        return Result.handleResult(() -> blogHttpService.findSensitiveByBlogId(blogId));

    }

}