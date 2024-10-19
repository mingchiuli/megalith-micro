package org.chiu.micro.blog.controller;

import org.chiu.micro.blog.req.BlogEditPushAllReq;
import org.chiu.micro.blog.rpc.AuthHttpServiceWrapper;
import org.chiu.micro.blog.service.BlogEditService;
import org.chiu.micro.blog.valid.PushAllValue;
import org.chiu.micro.blog.vo.BlogEditVo;
import org.chiu.micro.common.dto.AuthRpcDto;
import org.chiu.micro.common.exception.AuthException;
import org.chiu.micro.common.lang.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/sys/blog/edit")
@Validated
public class BlogEditController {

    private final BlogEditService blogEditService;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public BlogEditController(BlogEditService blogEditService, AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.blogEditService = blogEditService;
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }

    @PostMapping("/push/all")
    public Result<Void> pushSaveBlog(@RequestBody @PushAllValue BlogEditPushAllReq blog) throws AuthException {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogEditService.pushAll(blog, authDto.userId()));
    }

    @GetMapping("/pull/echo")
    public Result<BlogEditVo> getEchoDetail(@RequestParam(value = "blogId", required = false) Long id) throws AuthException {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogEditService.findEdit(id, authDto.userId()));
    }
}
