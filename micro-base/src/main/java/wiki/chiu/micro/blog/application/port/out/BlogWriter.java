package wiki.chiu.micro.blog.application.port.out;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.domain.Blog;

public interface BlogWriter {

    void recoverDeletedBlog(Blog blog, BlogEventContext event);

    void deleteByIds(List<BlogDeletionTarget> deleted, BlogEventContext event);

    void incrementViews(Long blogId);
}
