package org.chiu.micro.blog.controller;

import org.chiu.micro.blog.vo.BlogEditVo;
import org.chiu.micro.blog.dto.AuthDto;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.req.BlogEditPushAllReq;
import org.chiu.micro.blog.rpc.wrapper.AuthHttpServiceWrapper;
import org.chiu.micro.blog.service.BlogEditService;
import org.chiu.micro.blog.valid.PushAllValue;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/sys/blog")
@Validated
public class BlogEditController {

    private final BlogEditService blogEditService;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;
  
    @PostMapping("/edit/push/all")
    public Result<Void> pushSaveBlog(@RequestBody @PushAllValue BlogEditPushAllReq blog,
                                     HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogEditService.pushAll(blog, authDto.getUserId()));
    }

    @GetMapping("/edit/pull/echo")
    public Result<BlogEditVo> getEchoDetail(@RequestParam(value = "blogId", required = false) Long id,
                                            HttpServletRequest request) {
        AuthDto authDto = authHttpServiceWrapper.getAuthentication(request.getHeader(HttpHeaders.AUTHORIZATION));
        return Result.success(() -> blogEditService.findEdit(id, authDto.getUserId()));
    }
}
