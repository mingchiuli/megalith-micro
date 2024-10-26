package wiki.chiu.micro.blog.service;


import wiki.chiu.micro.blog.vo.BlogSensitiveContentVo;

public interface BlogSensitiveService {

    BlogSensitiveContentVo findByBlogId(Long blogId);
}