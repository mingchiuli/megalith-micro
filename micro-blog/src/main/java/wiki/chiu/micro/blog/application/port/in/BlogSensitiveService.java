package wiki.chiu.micro.blog.application.port.in;

import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;

public interface BlogSensitiveService {

    BlogSensitiveContentRpcVo findByBlogId(Long blogId);
}
