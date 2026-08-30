package wiki.chiu.micro.blog.application.port.out;

import java.util.List;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.domain.BlogSensitiveContentEntity;

public interface BlogWriter {

  void saveOrUpdate(
      BlogEntity blog,
      Long expectedRevision,
      List<Long> existingSensitiveIds,
      List<BlogSensitiveContentEntity> sensitiveContents,
      BlogEventContext event);

  void recoverDeletedBlog(BlogEntity blog, BlogEventContext event);

  void deleteByIds(List<BlogEntity> deleted, List<Long> sensitiveIds, BlogEventContext event);

  void incrementViews(Long blogId);
}
