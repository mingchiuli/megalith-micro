package wiki.chiu.micro.exhibit.controller;

import wiki.chiu.micro.cache.annotation.Checker;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.exhibit.checker.handler.DetailHandler;
import wiki.chiu.micro.exhibit.checker.handler.ListPageHandler;
import wiki.chiu.micro.exhibit.service.BlogService;
import wiki.chiu.micro.exhibit.vo.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-26 5:30 pm
 */
@RestController
@RequestMapping(value = "/public/blog")
@Validated
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/info/{blogId}")
    @Checker(handler = DetailHandler.class)
    public Result<BlogExhibitVo> getBlogDetail(@PathVariable Long blogId, AuthInfo authInfo) {
        return Result.success(() -> blogService.getBlogDetail(authInfo.roles(), blogId, authInfo.userId()));
    }

    @GetMapping("/page/{currentPage}")
    @Checker(handler = ListPageHandler.class)
    public Result<PageAdapter<BlogDescriptionVo>> getPage(@PathVariable Integer currentPage) {
        return Result.success(() -> blogService.findPage(currentPage));
    }

    @GetMapping("/secret/{blogId}")
    @Checker(handler = DetailHandler.class)
    public Result<BlogExhibitVo> getLockedBlog(@PathVariable Long blogId,
                                               @RequestParam(value = "readToken") String token) {
        return Result.success(blogService.getLockedBlog(blogId, token));
    }

    @GetMapping("/token/{blogId}")
    @Checker(handler = DetailHandler.class)
    public Result<Boolean> checkReadToken(@PathVariable Long blogId,
                                          @RequestParam(value = "readToken") String token) {
        return Result.success(() -> blogService.checkToken(blogId, token));
    }

    @GetMapping("/status/{blogId}")
    @Checker(handler = DetailHandler.class)
    public Result<Integer> getBlogStatus(@PathVariable Long blogId, AuthInfo authInfo) {
        return Result.success(() -> blogService.getBlogStatus(authInfo.roles(), blogId, authInfo.userId()));
    }

    @GetMapping("/stat")
    public Result<VisitStatisticsVo> getVisitStatistics() {
        return Result.success(blogService::getVisitStatistics);
    }

    @GetMapping("/scores")
    public Result<List<BlogHotReadVo>> getScoreBlogs() {
        return Result.success(blogService::getScoreBlogs);
    }

}
