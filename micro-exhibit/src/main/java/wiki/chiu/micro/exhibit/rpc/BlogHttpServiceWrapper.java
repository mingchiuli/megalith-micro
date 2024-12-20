package wiki.chiu.micro.exhibit.rpc;


import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.dto.BlogSensitiveContentRpcDto;
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

    public BlogEntityRpcDto findById(Long blogId) {
        return Result.handleResult(() -> blogHttpService.findById(blogId));
    }

    public List<BlogEntityRpcDto> findAllById(List<Long> ids) {
        return Result.handleResult(() -> blogHttpService.findAllById(ids));

    }

    public List<Integer> getYears() {
        return Result.handleResult(blogHttpService::getYears);

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

    public PageAdapter<BlogEntityRpcDto> findPage(Integer pageNo, Integer pageSize) {
        return Result.handleResult(() -> blogHttpService.findPage(pageNo, pageSize));

    }

    public PageAdapter<BlogEntityRpcDto> findPageByCreatedBetween(Integer pageNo, Integer pageSize, LocalDateTime start, LocalDateTime end) {
        return Result.handleResult(() -> blogHttpService.findPageByCreatedBetween(pageNo, pageSize, start, end));

    }

    public Long countByCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return Result.handleResult(() -> blogHttpService.countByCreatedBetween(start, end));

    }

    public long countByCreatedGreaterThanEqual(LocalDateTime created) {
        return Result.handleResult(() -> blogHttpService.countByCreatedGreaterThanEqual(created));

    }

    public long getPageCountYear(LocalDateTime created, LocalDateTime start, LocalDateTime end) {
        return Result.handleResult(() -> blogHttpService.getPageCountYear(created, start, end));

    }

    public BlogSensitiveContentRpcDto findSensitiveByBlogId(Long blogId) {
        return Result.handleResult(() -> blogHttpService.findSensitiveByBlogId(blogId));

    }

}