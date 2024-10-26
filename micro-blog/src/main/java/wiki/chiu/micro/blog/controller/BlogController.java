package wiki.chiu.micro.blog.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.rpc.AuthHttpServiceWrapper;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.valid.BlogSaveValue;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
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
        AuthRpcDto AuthRpcDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.saveOrUpdate(blog, AuthRpcDto.userId()));
    }

    @PostMapping("/delete")
    public Result<Void> deleteBlogs(@RequestBody @NotEmpty List<Long> ids) throws AuthException {
        AuthRpcDto AuthRpcDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.deleteBatch(ids, AuthRpcDto.userId(), AuthRpcDto.roles()));
    }

    @GetMapping("/lock/{blogId}")
    public Result<String> setBlogToken(@PathVariable Long blogId) throws AuthException {
        AuthRpcDto AuthRpcDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.setBlogToken(blogId, AuthRpcDto.userId()));
    }

    @GetMapping("/blogs")
    public Result<PageAdapter<BlogEntityVo>> getAllBlogs(@RequestParam(defaultValue = "1") Integer currentPage,
                                                         @RequestParam(defaultValue = "5") Integer size,
                                                         @RequestParam(required = false) String keywords) {
        return Result.success(() -> blogService.findAllBlogs(currentPage, size, keywords));
    }

    @GetMapping("/deleted")
    public Result<PageAdapter<BlogDeleteVo>> getDeletedBlogs(@RequestParam Integer currentPage,
                                                             @RequestParam Integer size) throws AuthException {
        AuthRpcDto AuthRpcDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.findDeletedBlogs(currentPage, size, AuthRpcDto.userId()));
    }

    @GetMapping("/recover/{idx}")
    public Result<Void> recoverDeletedBlog(@PathVariable Integer idx) throws AuthException {
        AuthRpcDto AuthRpcDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogService.recoverDeletedBlog(idx, AuthRpcDto.userId()));
    }

    @PostMapping(value = "/oss/upload")
    public SseEmitter uploadOss(@RequestBody MultipartFile image) throws AuthException {
        AuthRpcDto AuthRpcDto = authHttpServiceWrapper.getAuthentication();
        return blogService.uploadOss(image, AuthRpcDto.userId());
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
