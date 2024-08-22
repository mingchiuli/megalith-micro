package org.chiu.micro.blog.provider;

import java.time.LocalDateTime;
import java.util.List;

import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.page.PageAdapter;
import org.chiu.micro.blog.service.BlogService;
import org.chiu.micro.blog.vo.BlogEntityRpcVo;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * BlogProvider
 */
@RestController
@RequestMapping(value = "/inner")
@RequiredArgsConstructor
@Validated
public class BlogProvider {

    private final BlogService blogService;

    @GetMapping("/blog/{blogId}")
    public Result<BlogEntityRpcVo> findById(@PathVariable Long blogId) {
        return Result.success(() -> blogService.findById(blogId));
    }

    @GetMapping("/blog/{blogId}/{userId}")
    public Result<BlogEntityRpcVo> findByIdAndUserId(@PathVariable Long blogId,
                                                     @PathVariable Long userId) {
        return Result.success(() -> blogService.findByIdAndUserId(blogId, userId));
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

    @GetMapping("/blog/ids/{pageNo}/{pageSize}")
    public Result<List<Long>> findIds(@PathVariable Integer pageNo,
                                      @PathVariable Integer pageSize) {
        return Result.success(() -> blogService.findIds(pageNo, pageSize));
    }

    @PostMapping("/blog/{blogId}")
    public Result<Void> setReadCount(@PathVariable Long blogId) {
        return Result.success(() -> blogService.setReadCount(blogId));
    }

    @GetMapping("/blog/status/{blogId}")
    public Result<Integer> findStatusById(@PathVariable Long blogId) {
        return Result.success(() -> blogService.findStatusById(blogId));
    }

    @PostMapping("/blog/page/{pageNo}/{pageSize}")
    public Result<PageAdapter<BlogEntityRpcVo>> findPage(@PathVariable Integer pageNo,
                                                         @PathVariable Integer pageSize) {
        return Result.success(() -> blogService.findPage(pageNo, pageSize));
    }

    @PostMapping("/blog/page/year/{pageNo}/{pageSize}/{start}/{end}")
    public Result<PageAdapter<BlogEntityRpcVo>> findPageByCreatedBetween(@PathVariable Integer pageNo,
                                                                         @PathVariable Integer pageSize,
                                                                         @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                                                         @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return Result.success(() -> blogService.findPageByCreatedBetween(pageNo, pageSize, start, end));
    }

    @GetMapping("/blog/count/{start}/{end}")
    public Result<Long> countByCreatedBetween(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                              @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  LocalDateTime end) {
        return Result.success(() -> blogService.countByCreatedBetween(start, end));
    }

    @GetMapping("/blog/page/count/year/{created}/{start}/{end}")
    public Result<Long> getPageCountYear(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,
                                         @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                         @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return Result.success(() -> blogService.getPageCountYear(created, start, end));
    }

    @GetMapping("/blog/count/until/{created}")
    public Result<Long> countByCreatedGreaterThanEqual(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created) {
        return Result.success(() -> blogService.countByCreatedGreaterThanEqual(created));
    }
}