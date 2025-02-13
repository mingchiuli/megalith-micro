package wiki.chiu.micro.blog.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.valid.BlogSaveValue;
import wiki.chiu.micro.blog.valid.BlogSysDownload;
import wiki.chiu.micro.blog.valid.BlogSysQuery;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import wiki.chiu.micro.common.rpc.config.AuthInfo;

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

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @PostMapping("/save")
    public Result<Void> saveOrUpdate(@RequestBody @BlogSaveValue BlogEntityReq blog, AuthInfo authInfo) {
        return Result.success(() -> blogService.saveOrUpdate(blog, authInfo.userId()));
    }

    @PostMapping("/delete")
    public Result<Void> deleteBlogs(@RequestBody @NotEmpty List<Long> ids, AuthInfo authInfo) {
        return Result.success(() -> blogService.deleteBatch(ids, authInfo.userId(), authInfo.roles()));
    }

    @GetMapping("/lock/{blogId}")
    public Result<String> setBlogToken(@PathVariable Long blogId, AuthInfo authInfo) {
        return Result.success(() -> blogService.setBlogToken(blogId, authInfo.userId()));
    }

    @GetMapping("/blogs")
    public Result<PageAdapter<BlogEntityVo>> getAllBlogs(@BlogSysQuery BlogQueryReq req, AuthInfo authInfo) {
        return Result.success(() -> blogService.findAllBlogs(req, authInfo.userId(), authInfo.roles()));
    }

    @GetMapping("/deleted")
    public Result<PageAdapter<BlogDeleteVo>> getDeletedBlogs(@RequestParam Integer currentPage,
                                                             @RequestParam Integer size,
                                                             AuthInfo authInfo) {
        return Result.success(() -> blogService.findDeletedBlogs(currentPage, size, authInfo.userId()));
    }

    @GetMapping("/recover/{idx}")
    public Result<Void> recoverDeletedBlog(@PathVariable Integer idx, AuthInfo authInfo) {
        return Result.success(() -> blogService.recoverDeletedBlog(idx, authInfo.userId()));
    }

    @PostMapping(value = "/oss/upload")
    public SseEmitter uploadOss(@RequestBody MultipartFile image, AuthInfo authInfo) {
        return blogService.uploadOss(image, authInfo.userId());
    }

    @GetMapping("/oss/delete")
    public Result<Void> deleteOss(@RequestParam @NotBlank String url) {
        return Result.success(() -> blogService.deleteOss(url));
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response, @BlogSysDownload BlogDownloadReq req, AuthInfo authInfo) {
        blogService.download(response, req, authInfo.userId(), authInfo.roles());
    }

}
