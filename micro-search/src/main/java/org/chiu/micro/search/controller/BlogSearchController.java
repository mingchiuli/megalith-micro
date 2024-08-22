package org.chiu.micro.search.controller;

import org.chiu.micro.search.vo.BlogEntityVo;
import org.chiu.micro.search.dto.AuthDto;
import org.chiu.micro.search.lang.Result;
import org.chiu.micro.search.page.PageAdapter;
import org.chiu.micro.search.rpc.wrapper.AuthHttpServiceWrapper;
import org.chiu.micro.search.service.BlogSearchService;
import org.chiu.micro.search.vo.BlogDocumentVo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * @author mingchiuli
 * @create 2022-11-30 8:48 pm
 */
@RestController
@RequestMapping(value = "/search")
@RequiredArgsConstructor
@Validated
public class BlogSearchController {

    private final BlogSearchService blogSearchService;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    @GetMapping("/public/blog")
    public Result<PageAdapter<BlogDocumentVo>> searchBlogs(@RequestParam(defaultValue = "-1") Integer currentPage,
                                                           @RequestParam Boolean allInfo,
                                                           @RequestParam(required = false) String year,
                                                           @RequestParam @Size(min = 1, max = 20) String keywords) {
        return Result.success(() -> blogSearchService.selectBlogsByES(currentPage, keywords, allInfo, year));
    }

    @GetMapping("/sys/blogs")
    public Result<PageAdapter<BlogEntityVo>> searchAllBlogs(@RequestParam(defaultValue = "1") Integer currentPage,
                                                            @RequestParam(defaultValue = "5") Integer size,
                                                            @RequestParam @Size(min = 1, max = 20) String keywords,
                                                            HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogSearchService.searchAllBlogs(keywords, currentPage, size, authDto.getUserId(), authDto.getRoles()));
    }

}
