package wiki.chiu.micro.blog.service;

import wiki.chiu.micro.blog.req.BlogEditPushAllReq;
import wiki.chiu.micro.blog.vo.BlogEditVo;

public interface BlogEditService {

    void pushAll(BlogEditPushAllReq blog, Long userId);

    BlogEditVo findEdit(Long id, Long userId);
}