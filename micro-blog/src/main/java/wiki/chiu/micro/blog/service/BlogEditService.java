package wiki.chiu.micro.blog.service;

import wiki.chiu.micro.blog.vo.BlogEditVo;

import java.util.List;

public interface BlogEditService {

    BlogEditVo findEdit(Long id, Long userId, List<String> roles);
}