package wiki.chiu.micro.blog.application.port.in;

import wiki.chiu.micro.blog.application.model.BlogSensitiveSpans;

public interface BlogSensitiveService {

    BlogSensitiveSpans findByBlogId(Long blogId);
}
