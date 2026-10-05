package wiki.chiu.micro.blog.application.port.out;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;

public interface BlogWriter {

    void saveOrUpdate(
        Blog blog,
        Long expectedRevision,
        List<Long> existingSensitiveIds,
        List<SensitiveContent> sensitiveContents,
        BlogEventContext event);

    void recoverDeletedBlog(Blog blog, BlogEventContext event);

    void deleteByIds(List<Blog> deleted, List<Long> sensitiveIds, BlogEventContext event);

    void incrementViews(Long blogId);
}
