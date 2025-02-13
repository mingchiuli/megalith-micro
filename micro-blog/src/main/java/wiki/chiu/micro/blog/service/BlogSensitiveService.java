package wiki.chiu.micro.blog.service;


import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;

public interface BlogSensitiveService {

    BlogSensitiveContentRpcVo findByBlogId(Long blogId);
}