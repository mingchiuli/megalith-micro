package org.chiu.micro.blog.provider;


import org.chiu.micro.blog.service.BlogSensitiveService;
import org.chiu.micro.blog.vo.BlogSensitiveContentVo;
import org.chiu.micro.common.lang.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/inner")
@Validated
public class BlogSensitiveProvider {

    private final BlogSensitiveService blogSensitiveService;

    public BlogSensitiveProvider(BlogSensitiveService blogSensitiveService) {
        this.blogSensitiveService = blogSensitiveService;
    }

    @GetMapping("/blog/sensitive/{blogId}")
    public Result<BlogSensitiveContentVo> findById(@PathVariable Long blogId) {
        return Result.success(() -> blogSensitiveService.findByBlogId(blogId));
    }

}
