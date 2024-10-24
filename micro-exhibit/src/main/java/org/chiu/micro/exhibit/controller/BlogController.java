package org.chiu.micro.exhibit.controller;

import org.chiu.micro.common.dto.AuthRpcDto;
import org.chiu.micro.common.lang.Result;
import org.chiu.micro.common.page.PageAdapter;
import org.chiu.micro.exhibit.bloom.Bloom;
import org.chiu.micro.exhibit.bloom.DetailHandler;
import org.chiu.micro.exhibit.bloom.ListPageHandler;
import org.chiu.micro.exhibit.rpc.AuthHttpServiceWrapper;
import org.chiu.micro.exhibit.service.BlogService;
import org.chiu.micro.exhibit.vo.BlogDescriptionVo;
import org.chiu.micro.exhibit.vo.BlogExhibitVo;
import org.chiu.micro.exhibit.vo.BlogHotReadVo;
import org.chiu.micro.exhibit.vo.VisitStatisticsVo;
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

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public BlogController(BlogService blogService, AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.blogService = blogService;
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }

    @GetMapping("/info/{blogId}")
    @Bloom(handler = DetailHandler.class)
    public Result<BlogExhibitVo> getBlogDetail(@PathVariable Long blogId) {

        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.getBlogDetail(authDto.roles(), blogId, authDto.userId()));
    }

    @GetMapping("/page/{currentPage}")
    @Bloom(handler = ListPageHandler.class)
    public Result<PageAdapter<BlogDescriptionVo>> getPage(@PathVariable Integer currentPage,
                                                          @RequestParam(required = false, defaultValue = "-2147483648") Integer year) {
        return Result.success(() -> blogService.findPage(currentPage, year));
    }

    @GetMapping("/secret/{blogId}")
    @Bloom(handler = DetailHandler.class)
    public Result<BlogExhibitVo> getLockedBlog(@PathVariable Long blogId,
                                               @RequestParam(value = "readToken") String token) {
        return Result.success(blogService.getLockedBlog(blogId, token));
    }

    @GetMapping("/token/{blogId}")
    @Bloom(handler = DetailHandler.class)
    public Result<Boolean> checkReadToken(@PathVariable Long blogId,
                                          @RequestParam(value = "readToken") String token) {
        return Result.success(() -> blogService.checkToken(blogId, token));
    }

    @GetMapping("/status/{blogId}")
    @Bloom(handler = DetailHandler.class)
    public Result<Integer> getBlogStatus(@PathVariable Long blogId) {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.getBlogStatus(authDto.roles(), blogId, authDto.userId()));
    }

    @GetMapping("/years")
    public Result<List<Integer>> searchYears() {
        return Result.success(blogService::searchYears);
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
