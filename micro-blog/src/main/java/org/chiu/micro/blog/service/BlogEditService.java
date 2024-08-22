package org.chiu.micro.blog.service;

import org.chiu.micro.blog.req.BlogEditPushAllReq;
import org.chiu.micro.blog.vo.BlogEditVo;

public interface BlogEditService {

    void pushAll(BlogEditPushAllReq blog, Long userId);

    BlogEditVo findEdit(Long id, Long userId);
}