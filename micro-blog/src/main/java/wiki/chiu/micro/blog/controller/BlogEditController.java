package wiki.chiu.micro.blog.controller;

import wiki.chiu.micro.blog.req.BlogEditPushAllReq;
import wiki.chiu.micro.blog.service.BlogEditService;
import wiki.chiu.micro.blog.valid.PushAllValue;
import wiki.chiu.micro.blog.vo.BlogEditVo;

import wiki.chiu.micro.common.lang.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.rpc.config.AuthInfo;

@RestController
@RequestMapping(value = "/sys/blog/edit")
@Validated
public class BlogEditController {

    private final BlogEditService blogEditService;

    public BlogEditController(BlogEditService blogEditService) {
        this.blogEditService = blogEditService;
    }

    @PostMapping("/push/all")
    public Result<Void> pushSaveBlog(@RequestBody @PushAllValue BlogEditPushAllReq blog, AuthInfo authInfo) {
        return Result.success(() -> blogEditService.pushAll(blog, authInfo.userId()));
    }

    @GetMapping("/pull/echo")
    public Result<BlogEditVo> getEchoDetail(@RequestParam(value = "blogId", required = false) Long id, AuthInfo authInfo) {
        return Result.success(() -> blogEditService.findEdit(id, authInfo.userId()));
    }
}
