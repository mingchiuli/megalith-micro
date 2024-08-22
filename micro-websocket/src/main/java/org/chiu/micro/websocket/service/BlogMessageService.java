package org.chiu.micro.websocket.service;

import org.chiu.micro.websocket.req.BlogEditPushActionReq;

public interface BlogMessageService {

    void pushAction(BlogEditPushActionReq req, Long userId);
}
