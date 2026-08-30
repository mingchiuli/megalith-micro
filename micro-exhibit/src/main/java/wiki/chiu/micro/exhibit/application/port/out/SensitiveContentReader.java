package wiki.chiu.micro.exhibit.application.port.out;

import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;

public interface SensitiveContentReader {

  BlogSensitiveContentRpcVo findSensitiveByBlogId(Long blogId);
}
