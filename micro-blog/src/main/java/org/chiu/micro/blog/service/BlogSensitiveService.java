package org.chiu.micro.blog.service;


import org.chiu.micro.blog.vo.BlogSensitiveContentVo;

public interface BlogSensitiveService {

    BlogSensitiveContentVo findByBlogId(Long blogId);
}