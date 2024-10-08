package org.chiu.micro.blog.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.chiu.micro.blog.dto.AuthDto;
import org.chiu.micro.blog.exception.AuthException;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.page.PageAdapter;
import org.chiu.micro.blog.req.BlogEntityReq;
import org.chiu.micro.blog.rpc.wrapper.AuthHttpServiceWrapper;
import org.chiu.micro.blog.service.BlogService;
import org.chiu.micro.blog.valid.BlogSaveValue;
import org.chiu.micro.blog.vo.BlogDeleteVo;
import org.chiu.micro.blog.vo.BlogEntityVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:28 pm
 */
@RestController
@RequestMapping(value = "/sys/blog")
@Validated
public class BlogController {

    private final BlogService blogService;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public BlogController(BlogService blogService, AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.blogService = blogService;
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }


    @PostMapping("/save")
    public Result<Void> saveOrUpdate(@RequestBody @BlogSaveValue BlogEntityReq blog) throws AuthException {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.saveOrUpdate(blog, authDto.userId()));
    }

    @PostMapping("/delete")
    public Result<Void> deleteBlogs(@RequestBody @NotEmpty List<Long> ids) throws AuthException {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.deleteBatch(ids, authDto.userId(), authDto.roles()));
    }

    @GetMapping("/lock/{blogId}")
    public Result<String> setBlogToken(@PathVariable Long blogId) throws AuthException {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.setBlogToken(blogId, authDto.userId()));
    }

    @GetMapping("/blogs")
    public Result<PageAdapter<BlogEntityVo>> getAllBlogs(@RequestParam(defaultValue = "1") Integer currentPage,
                                                         @RequestParam(defaultValue = "5") Integer size,
                                                         @RequestParam(required = false) String keywords) throws AuthException {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.findAllBlogs(currentPage, size, authDto.userId(), keywords));
    }

    @GetMapping("/deleted")
    public Result<PageAdapter<BlogDeleteVo>> getDeletedBlogs(@RequestParam Integer currentPage,
                                                             @RequestParam Integer size) throws AuthException {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.findDeletedBlogs(currentPage, size, authDto.userId()));
    }

    @GetMapping("/recover/{idx}")
    public Result<Void> recoverDeletedBlog(@PathVariable Integer idx) throws AuthException {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.recoverDeletedBlog(idx, authDto.userId()));
    }

    @PostMapping(value = "/oss/upload")
    public SseEmitter uploadOss(@RequestBody MultipartFile image) throws AuthException {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication();
        return blogService.uploadOss(image, authDto.userId());
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
