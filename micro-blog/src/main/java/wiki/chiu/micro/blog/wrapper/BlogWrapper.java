package wiki.chiu.micro.blog.wrapper;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.blog.convertor.BlogDeleteDtoConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityConvertor;
import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.service.BlogAccessPolicy;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogChangedMessage;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.outbox.OutboxProducer;
import wiki.chiu.micro.outbox.OutboxService;

@Component
public class BlogWrapper {

  private final BlogRepository blogs;
  private final BlogSensitiveContentRepository sensitiveContents;
  private final BlogAccessPolicy accessPolicy;
  private final OutboxService outbox;

  public BlogWrapper(
      BlogRepository blogs,
      BlogSensitiveContentRepository sensitiveContents,
      BlogAccessPolicy accessPolicy,
      OutboxService outbox) {
    this.blogs = blogs;
    this.sensitiveContents = sensitiveContents;
    this.accessPolicy = accessPolicy;
    this.outbox = outbox;
  }

  @Transactional
  public BlogEntity saveOrUpdate(
      BlogEntityReq request,
      Long userId,
      List<DataPermissionEnum> dataPermissions,
      List<BlogSensitiveContentEntity> newSensitiveContents) {
    BlogEntity current =
        request
            .id()
            .map(
                blogId -> {
                  BlogEntity existing =
                      blogs
                          .findByIdForUpdate(blogId)
                          .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
                  accessPolicy.requireEdit(existing, userId, dataPermissions);
                  return existing;
                })
            .orElseGet(() -> BlogEntity.builder().userId(userId).readCount(0L).build());
    BlogEntity saved = blogs.save(BlogEntityConvertor.convert(request, current));

    if (request.id().isPresent()) {
      sensitiveContents.deleteAllById(
          sensitiveContents.findByBlogId(saved.getId()).stream()
              .map(BlogSensitiveContentEntity::getId)
              .toList());
    }
    newSensitiveContents.forEach(item -> item.setBlogId(saved.getId()));
    sensitiveContents.saveAll(newSensitiveContents);

    enqueue(
        request.id().isPresent() ? BlogOperateEnum.UPDATE : BlogOperateEnum.CREATE, userId, saved);
    return saved;
  }

  @Transactional
  public void recoverDeletedBlog(BlogDeleteDto deletedBlog, Long userId) {
    BlogEntity saved = blogs.save(BlogEntityConvertor.convertRecover(deletedBlog));
    enqueue(BlogOperateEnum.CREATE, userId, saved);
  }

  @Transactional
  public List<BlogDeleteDto> deleteByIds(
      List<Long> ids, Long userId, List<DataPermissionEnum> dataPermissions) {
    List<BlogEntity> deleted =
        blogs.findAllByIdForUpdate(ids).stream()
            .filter(blog -> accessPolicy.canDelete(blog, userId, dataPermissions))
            .toList();
    deleted.forEach(
        blog -> blog.setEventRevision(Optional.ofNullable(blog.getEventRevision()).orElse(0L) + 1));

    List<Long> deletedIds = deleted.stream().map(BlogEntity::getId).toList();
    List<Long> sensitiveIds =
        sensitiveContents.findByBlogIdIn(deletedIds).stream()
            .map(BlogSensitiveContentEntity::getId)
            .toList();
    blogs.deleteAllById(deletedIds);
    sensitiveContents.deleteAllById(sensitiveIds);
    blogs.flush();

    deleted.forEach(blog -> enqueue(BlogOperateEnum.REMOVE, userId, blog));
    return deleted.stream().map(BlogDeleteDtoConvertor::convert).toList();
  }

  @Transactional
  public void incrementViews(Long blogId) {
    blogs.setReadCount(blogId);
  }

  private void enqueue(BlogOperateEnum operation, Long operatorUserId, BlogEntity blog) {
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
    long totalCount = blogs.count();
    Long newerOrSameCount =
        BlogOperateEnum.UPDATE.equals(operation)
            ? blogs.countByCreatedGreaterThanEqual(blog.getCreated())
            : null;
    outbox.enqueue(
        OutboxProducer.BLOG,
        "BLOG",
        blog.getId(),
        eventId ->
            new BlogChangedMessage(
                eventId,
                operation.getCode(),
                blog.getEventRevision(),
                operatorUserId,
                snapshot,
                totalCount,
                newerOrSameCount));
  }
}
