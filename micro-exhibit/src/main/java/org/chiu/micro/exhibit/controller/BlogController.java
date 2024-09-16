package org.chiu.micro.exhibit.controller;

import org.chiu.micro.exhibit.bloom.DetailHandler;
import org.chiu.micro.exhibit.bloom.ListPageHandler;
import org.chiu.micro.exhibit.dto.AuthDto;
import org.chiu.micro.exhibit.vo.BlogDescriptionVo;
import org.chiu.micro.exhibit.vo.VisitStatisticsVo;
import org.chiu.micro.exhibit.bloom.Bloom;
import org.chiu.micro.exhibit.service.BlogService;
import org.chiu.micro.exhibit.lang.Result;
import org.chiu.micro.exhibit.page.PageAdapter;
import org.chiu.micro.exhibit.rpc.wrapper.AuthHttpServiceWrapper;
import org.chiu.micro.exhibit.vo.BlogExhibitVo;
import org.chiu.micro.exhibit.vo.BlogHotReadVo;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Collections;

/**
 * @author mingchiuli
 * @create 2022-11-26 5:30 pm
 */
@RestController
@RequestMapping(value = "/public/blog")
@RequiredArgsConstructor
@Validated
public class BlogController {

    private final BlogService blogService;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    @GetMapping("/info/{blogId}")
    @Bloom(handler = DetailHandler.class)
    public Result<BlogExhibitVo> getBlogDetail(@PathVariable Long blogId) {
        
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        Long userId = authDto.getUserId();
        List<String> roles = authDto.getRoles();
        return Result.success(() -> blogService.getBlogDetail(roles, blogId, userId));
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
        
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        Long userId = authDto.getUserId();
        List<String> roles = authDto.getRoles();
        return Result.success(() -> blogService.getBlogStatus(roles, blogId, userId));
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
