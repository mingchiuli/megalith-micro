package wiki.chiu.micro.blog.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.outbox.OutboxProducer;
import wiki.chiu.micro.outbox.OutboxService;

@Component
public class BlogWrapper {

  private final BlogRepository blogs;
  private final BlogSensitiveContentRepository sensitiveContents;
  private final OutboxService outbox;

  public BlogWrapper(
      BlogRepository blogs,
      BlogSensitiveContentRepository sensitiveContents,
      OutboxService outbox) {
    this.blogs = blogs;
    this.sensitiveContents = sensitiveContents;
    this.outbox = outbox;
  }

  @Transactional
  public void saveOrUpdate(
      BlogEntity blog,
      Long expectedRevision,
      List<Long> existingSensitiveIds,
      List<BlogSensitiveContentEntity> newSensitiveContents,
      BlogEventContext event) {
    BlogEntity persisted =
        expectedRevision == null ? blogs.save(blog) : update(blog, expectedRevision);

    sensitiveContents.deleteAllByIdInBatch(existingSensitiveIds);
    newSensitiveContents.forEach(item -> item.setBlogId(persisted.getId()));
    sensitiveContents.saveAll(newSensitiveContents);
    enqueue(persisted, event);
  }

  @Transactional
  public void recoverDeletedBlog(BlogEntity blog, BlogEventContext event) {
    enqueue(blogs.save(blog), event);
  }

  @Transactional
  public void deleteByIds(
      List<BlogEntity> deleted, List<Long> sensitiveIds, BlogEventContext event) {
    deleted.forEach(
        blog -> {
          long expectedRevision = blog.getEventRevision() - 1;
          if (blogs.deleteByIdAndEventRevision(blog.getId(), expectedRevision) != 1) {
            throw revisionConflict(blog.getId());
          }
        });
    sensitiveContents.deleteAllByIdInBatch(sensitiveIds);
    deleted.forEach(blog -> enqueue(blog, event));
  }

  @Transactional
  public void incrementViews(Long blogId) {
    blogs.setReadCount(blogId);
  }

  private BlogEntity update(BlogEntity blog, Long expectedRevision) {
    int updated =
        blogs.updateByIdAndEventRevision(
            blog.getId(),
            expectedRevision,
            blog.getEventRevision(),
            blog.getTitle(),
            blog.getDescription(),
            blog.getContent(),
            blog.getStatus(),
            blog.getLink(),
            blog.getUpdated());
    if (updated != 1) {
      throw revisionConflict(blog.getId());
    }
    return blog;
  }

  private BaseException revisionConflict(Long blogId) {
    return new BaseException(CommonErrorCode.CONFLICT, "blog revision conflict: " + blogId);
  }

  private void enqueue(BlogEntity blog, BlogEventContext event) {
    BlogSnapshot snapshot =
        new BlogSnapshot(
            blog.getId(),
            blog.getUserId(),
            blog.getTitle(),
            blog.getDescription(),
            blog.getContent(),
            blog.getCreated(),
            blog.getUpdated(),
            blog.getStatus(),
            blog.getLink(),
            blog.getReadCount(),
            blog.getEventRevision());
    outbox.enqueue(
        OutboxProducer.BLOG,
        "BLOG",
        blog.getId(),
        eventId ->
            new BlogChangedMessage(
                eventId,
                event.operation().getCode(),
                blog.getEventRevision(),
                event.operatorUserId(),
                snapshot,
                event.totalCount(),
                event.newerOrSameCount()));
  }
}
