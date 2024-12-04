package wiki.chiu.micro.blog.provider;

import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BlogProvider
 */
@RestController
@RequestMapping(value = "/inner")
@Validated
public class BlogProvider {

    private final BlogService blogService;

    public BlogProvider(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/blog/{blogId}")
    public Result<BlogEntityRpcVo> findById(@PathVariable Long blogId) {
        return Result.success(() -> blogService.findById(blogId));
    }

    @PostMapping("/blog/batch")
    public Result<List<BlogEntityRpcVo>> findAllById(@RequestBody List<Long> ids) {
        return Result.success(() -> blogService.findAllById(ids));
    }

    @GetMapping("/blog/years")
    public Result<List<Integer>> getYears() {
        return Result.success(blogService::getYears);
    }

    @GetMapping("/blog/count")
    public Result<Long> count() {
        return Result.success(blogService::count);
    }

    @PostMapping("/blog/{blogId}")
    public Result<Void> setReadCount(@PathVariable Long blogId) {
        return Result.success(() -> blogService.setReadCount(blogId));
    }

    @GetMapping("/blog/status/{blogId}")
    public Result<Integer> findStatusById(@PathVariable Long blogId) {
        return Result.success(() -> blogService.findStatusById(blogId));
    }

    @PostMapping("/blog/page")
    public Result<PageAdapter<BlogEntityRpcVo>> findPage(@RequestParam Integer pageNo,
                                                         @RequestParam Integer pageSize) {
        return Result.success(() -> blogService.findPage(pageNo, pageSize));
    }

    @PostMapping("/blog/page/year")
    public Result<PageAdapter<BlogEntityRpcVo>> findPageByCreatedBetween(@RequestParam Integer pageNo,
                                                                         @RequestParam Integer pageSize,
                                                                         @RequestParam LocalDateTime start,
                                                                         @RequestParam LocalDateTime end) {
        return Result.success(() -> blogService.findPageByCreatedBetween(pageNo, pageSize, start, end));
    }

    @GetMapping("/blog/count")
    public Result<Long> countByCreatedBetween(@RequestParam LocalDateTime start,
                                              @RequestParam LocalDateTime end) {
        return Result.success(() -> blogService.countByCreatedBetween(start, end));
    }

    @GetMapping("/blog/page/count/year")
    public Result<Long> getPageCountYear(@RequestParam LocalDateTime created,
                                         @RequestParam LocalDateTime start,
                                         @RequestParam LocalDateTime end) {
        return Result.success(() -> blogService.getPageCountYear(created, start, end));
    }

    @GetMapping("/blog/count/until")
    public Result<Long> countByCreatedGreaterThanEqual(@RequestParam LocalDateTime created) {
        return Result.success(() -> blogService.countByCreatedGreaterThanEqual(created));
    }
}