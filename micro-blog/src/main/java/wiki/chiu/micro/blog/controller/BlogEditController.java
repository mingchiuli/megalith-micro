package wiki.chiu.micro.blog.controller;

import wiki.chiu.micro.blog.service.BlogEditService;
import wiki.chiu.micro.blog.vo.BlogEditVo;

import wiki.chiu.micro.common.lang.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;

@RestController
@RequestMapping(value = "/sys/blog/edit")
@Validated
public class BlogEditController {

    private final BlogEditService blogEditService;

    public BlogEditController(BlogEditService blogEditService) {
        this.blogEditService = blogEditService;
    }

    @GetMapping("/pull/echo")
    public Result<BlogEditVo> getEchoDetail(@RequestParam(value = "blogId", required = false) Long id, AuthInfo authInfo) {
        return Result.success(() -> blogEditService.findEdit(id, authInfo.userId(), authInfo.roles()));
    }
}
