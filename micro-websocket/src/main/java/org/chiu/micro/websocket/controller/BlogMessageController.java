package org.chiu.micro.websocket.controller;

import org.chiu.micro.websocket.req.BlogEditPushActionReq;
import org.chiu.micro.websocket.service.BlogMessageService;
import org.chiu.micro.websocket.utils.SecurityUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class BlogMessageController {

    private final BlogMessageService blogMessageService;

    @MessageMapping("/edit/ws/push/action")
    public void pushAction(@RequestBody @Valid BlogEditPushActionReq req) {
        Long userId = SecurityUtils.getLoginUserId();
        blogMessageService.pushAction(req, userId);
    }
}
