package org.chiu.micro.blog.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.chiu.micro.blog.service.BlogService;
import org.chiu.micro.blog.valid.BlogSaveValue;
import org.chiu.micro.blog.vo.BlogDeleteVo;
import org.chiu.micro.blog.vo.BlogEntityVo;
import org.chiu.micro.blog.req.BlogEntityReq;
import org.chiu.micro.blog.rpc.wrapper.AuthHttpServiceWrapper;

import java.util.List;

import org.chiu.micro.blog.dto.AuthDto;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.page.PageAdapter;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:28 pm
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/sys/blog")
@Validated
public class BlogController {

    private final BlogService blogService;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;


    @PostMapping("/save")
    public Result<Void> saveOrUpdate(@RequestBody @BlogSaveValue BlogEntityReq blog,
                                     HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogService.saveOrUpdate(blog, authDto.getUserId()));
    }

    @PostMapping("/delete")
    public Result<Void> deleteBlogs(@RequestBody @NotEmpty List<Long> ids,
                                    HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogService.deleteBatch(ids, authDto.getUserId(), authDto.getRoles()));
    }

    @GetMapping("/lock/{blogId}")
    public Result<String> setBlogToken(@PathVariable Long blogId,
                                       HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogService.setBlogToken(blogId, authDto.getUserId()));
    }

    @GetMapping("/blogs")
    public Result<PageAdapter<BlogEntityVo>> getAllBlogs(@RequestParam(defaultValue = "1") Integer currentPage,
                                                         @RequestParam(defaultValue = "5") Integer size,
                                                         @RequestParam(required = false) String keywords,
                                                         HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogService.findAllBlogs(currentPage, size, authDto.getUserId(), keywords));
    }

    @GetMapping("/deleted")
    public Result<PageAdapter<BlogDeleteVo>> getDeletedBlogs(@RequestParam Integer currentPage,
                                                             @RequestParam Integer size,
                                                             HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogService.findDeletedBlogs(currentPage, size, authDto.getUserId()));
    }

    @GetMapping("/recover/{idx}")
    public Result<Void> recoverDeletedBlog(@PathVariable Integer idx,
                                           HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogService.recoverDeletedBlog(idx, authDto.getUserId()));
    }

    @PostMapping(value = "/oss/upload")
    public SseEmitter uploadOss(@RequestBody MultipartFile file,
                                HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return blogService.uploadOss(file, authDto.getUserId());
    }

    @GetMapping("/oss/delete")
    public Result<Void> deleteOss(@RequestParam @NotBlank String url) {
        return Result.success(() -> blogService.deleteOss(url));
    }

    @GetMapping("/download")
    public Result<Void> download(HttpServletResponse response) {
        blogService.download(response);
        return Result.success();
    }

}
