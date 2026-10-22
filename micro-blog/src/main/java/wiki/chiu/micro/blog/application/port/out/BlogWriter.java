package wiki.chiu.micro.blog.application.port.out;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.domain.Blog;

public interface BlogWriter {

    void recoverDeletedBlog(Blog blog, BlogEventContext event);

    void deleteByIds(List<Blog> deleted, List<Long> sensitiveIds, BlogEventContext event);

    void incrementViews(Long blogId);
}
