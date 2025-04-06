package wiki.chiu.micro.blog.service;

import wiki.chiu.micro.blog.vo.BlogEditVo;

public interface BlogEditService {

    BlogEditVo findEdit(Long id, Long userId);
}