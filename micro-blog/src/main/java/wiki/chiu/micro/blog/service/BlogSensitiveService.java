package wiki.chiu.micro.blog.service;

import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;

public interface BlogSensitiveService {

  BlogSensitiveContentRpcVo findByBlogId(Long blogId);
}
