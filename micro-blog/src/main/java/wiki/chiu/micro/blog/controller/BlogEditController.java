package wiki.chiu.micro.blog.controller;

import wiki.chiu.micro.blog.req.BlogEditPushAllReq;
import wiki.chiu.micro.blog.rpc.AuthHttpServiceWrapper;
import wiki.chiu.micro.blog.service.BlogEditService;
import wiki.chiu.micro.blog.valid.PushAllValue;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.lang.Result;
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
