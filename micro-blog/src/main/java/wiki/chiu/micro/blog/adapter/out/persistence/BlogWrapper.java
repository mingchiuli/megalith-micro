package wiki.chiu.micro.blog.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.outbox.application.OutboxService;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

@Component
public class BlogWrapper implements BlogWriter {

    private final BlogRepository blogs;
    private final BlogSensitiveContentRepository sensitiveContents;
    private final OutboxService outbox;
    private final BlogMaintenanceMode maintenance;

    public BlogWrapper(
        BlogRepository blogs,
        BlogSensitiveContentRepository sensitiveContents,
        OutboxService outbox,
        BlogMaintenanceMode maintenance) {
        this.blogs = blogs;
        this.sensitiveContents = sensitiveContents;
        this.outbox = outbox;
        this.maintenance = maintenance;
    }

    @Transactional
    @Override
    public void saveOrUpdate(
        Blog blog,
        Long expectedRevision,
        List<Long> existingSensitiveIds,
        List<SensitiveContent> newSensitiveContents,
        BlogEventContext event) {
        maintenance.requireWritable();
        Blog persisted =
            expectedRevision == null ? create(blog) : update(blog, expectedRevision);

        sensitiveContents.deleteAllByIdInBatch(existingSensitiveIds);
        sensitiveContents.saveAll(
            newSensitiveContents.stream()
                .map(content -> BlogPersistenceMapper.toEntity(content.onBlog(persisted.id())))
                .toList());
        enqueue(persisted, event);
    }

    @Transactional
    @Override
    public void recoverDeletedBlog(Blog blog, BlogEventContext event) {
        maintenance.requireWritable();
        enqueue(BlogPersistenceMapper.toDomain(blogs.save(BlogPersistenceMapper.toEntity(blog))), event);
    }

    @Transactional
    @Override
    public void deleteByIds(List<Blog> deleted, List<Long> sensitiveIds, BlogEventContext event) {
        maintenance.requireWritable();
        deleted.forEach(
            blog -> {
                long expectedRevision = blog.eventRevision() - 1;
                if (blogs.deleteByIdAndEventRevision(blog.id(), expectedRevision) != 1) {
                    throw revisionConflict(blog.id());
                }
            });
        sensitiveContents.deleteAllByIdInBatch(sensitiveIds);
        deleted.forEach(blog -> enqueue(blog, event));
    }

    @Transactional
    @Override
    public void incrementViews(Long blogId) {
        blogs.setReadCount(blogId);
    }

    private Blog create(Blog blog) {
        return BlogPersistenceMapper.toDomain(blogs.save(BlogPersistenceMapper.toEntity(blog)));
    }

    private Blog update(Blog blog, Long expectedRevision) {
        int updated =
            blogs.updateByIdAndEventRevision(
                blog.id(),
                expectedRevision,
                blog.eventRevision(),
                blog.title(),
                blog.description(),
                blog.content(),
                blog.status(),
                blog.link(),
                blog.updated());
        if (updated != 1) {
            throw revisionConflict(blog.id());
        }
        return blog;
    }

    private BaseException revisionConflict(Long blogId) {
        return new BaseException(CommonErrorCode.CONFLICT, "blog revision conflict: " + blogId);
    }

    private void enqueue(Blog blog, BlogEventContext event) {
        BlogSnapshot snapshot = blog.snapshot();
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            blog.id(),
            eventId ->
                new BlogChangedMessage(
                    eventId,
                    event.operation().getCode(),
                    blog.eventRevision(),
                    event.operatorUserId(),
                    snapshot));
    }
}
