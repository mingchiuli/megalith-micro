package org.chiu.micro.websocket.controller;

import jakarta.validation.Valid;
import org.chiu.micro.websocket.req.BlogEditPushActionReq;
import org.chiu.micro.websocket.service.BlogMessageService;
import org.chiu.micro.websocket.utils.SecurityUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class BlogMessageController {

    private final BlogMessageService blogMessageService;

    public BlogMessageController(BlogMessageService blogMessageService) {
        this.blogMessageService = blogMessageService;
    }

    @MessageMapping("/edit/ws/push/action")
    public void pushAction(@RequestBody @Valid BlogEditPushActionReq req) {
        Long userId = SecurityUtils.getLoginUserId();
        blogMessageService.pushAction(req, userId);
    }
}
