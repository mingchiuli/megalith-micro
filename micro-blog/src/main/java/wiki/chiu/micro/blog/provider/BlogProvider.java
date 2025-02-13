package wiki.chiu.micro.blog.provider;

import wiki.chiu.micro.blog.service.BlogSensitiveService;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BlogProvider
 */
@RestController
@RequestMapping(value = "/inner/blog")
@Validated
public class BlogProvider implements BlogHttpService {

    private final BlogService blogService;

    private final BlogSensitiveService blogSensitiveService;

    public BlogProvider(BlogService blogService, BlogSensitiveService blogSensitiveService) {
        this.blogService = blogService;
        this.blogSensitiveService = blogSensitiveService;
    }

    @GetMapping("/{blogId}")
    public Result<BlogEntityRpcVo> findById(@PathVariable Long blogId) {
        return Result.success(() -> blogService.findById(blogId));
    }

    @PostMapping("/batch")
    public Result<List<BlogEntityRpcVo>> findAllById(@RequestBody List<Long> ids) {
        return Result.success(() -> blogService.findAllById(ids));
    }

    @GetMapping("/years")
    public Result<List<Integer>> getYears() {
        return Result.success(blogService::getYears);
    }

    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(blogService::count);
    }

    @PostMapping("/{blogId}")
    public Result<Void> setReadCount(@PathVariable Long blogId) {
        return Result.success(() -> blogService.setReadCount(blogId));
    }

    @GetMapping("/status/{blogId}")
    public Result<Integer> findStatusById(@PathVariable Long blogId) {
        return Result.success(() -> blogService.findStatusById(blogId));
    }

    @PostMapping("/page")
    public Result<PageAdapter<BlogEntityRpcVo>> findPage(@RequestParam Integer pageNo,
                                                         @RequestParam Integer pageSize) {
        return Result.success(() -> blogService.findPage(pageNo, pageSize));
    }

    @PostMapping("/page/year")
    public Result<PageAdapter<BlogEntityRpcVo>> findPageByCreatedBetween(@RequestParam Integer pageNo,
                                                                         @RequestParam Integer pageSize,
                                                                         @RequestParam LocalDateTime start,
                                                                         @RequestParam LocalDateTime end) {
        return Result.success(() -> blogService.findPageByCreatedBetween(pageNo, pageSize, start, end));
    }

    @GetMapping("/count/year")
    public Result<Long> countByCreatedBetween(@RequestParam LocalDateTime start,
                                              @RequestParam LocalDateTime end) {
        return Result.success(() -> blogService.countByCreatedBetween(start, end));
    }

    @GetMapping("/page/count/year")
    public Result<Long> getPageCountYear(@RequestParam LocalDateTime created,
                                         @RequestParam LocalDateTime start,
                                         @RequestParam LocalDateTime end) {
        return Result.success(() -> blogService.getPageCountYear(created, start, end));
    }

    @GetMapping("/count/until")
    public Result<Long> countByCreatedGreaterThanEqual(@RequestParam LocalDateTime created) {
        return Result.success(() -> blogService.countByCreatedGreaterThanEqual(created));
    }

    @GetMapping("/sensitive/{blogId}")
    public Result<BlogSensitiveContentRpcVo> findSensitiveByBlogId(@PathVariable Long blogId) {
        return Result.success(() -> blogSensitiveService.findByBlogId(blogId));
    }
}