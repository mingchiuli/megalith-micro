package wiki.chiu.micro.blog.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

/**
 * Deletion and recovery writer kept separate from authoring transactions.
 */
@Component
public class BlogWrapper implements BlogWriter {

    private final BlogRepository blogs;
    private final BlogRevisionRepository revisions;
    private final BlogDraftRepository drafts;
    private final BlogDraftSensitiveContentRepository draftSensitive;
    private final BlogRevisionSensitiveContentRepository revisionSensitive;
    private final OutboxEvents outbox;
    private final BlogMaintenanceMode maintenance;

    public BlogWrapper(
        BlogRepository blogs,
        BlogRevisionRepository revisions,
        BlogDraftRepository drafts,
        BlogDraftSensitiveContentRepository draftSensitive,
        BlogRevisionSensitiveContentRepository revisionSensitive,
        OutboxEvents outbox,
        BlogMaintenanceMode maintenance) {
        this.blogs = blogs;
        this.revisions = revisions;
        this.drafts = drafts;
        this.draftSensitive = draftSensitive;
        this.revisionSensitive = revisionSensitive;
        this.outbox = outbox;
        this.maintenance = maintenance;
    }

    @Transactional
    @Override
    public void recoverDeletedBlog(Blog blog, BlogEventContext event) {
        maintenance.requireWritable();
        LocalDateTime now = LocalDateTime.now();
        int accessMode =
            Objects.equals(blog.status(), BlogStatusEnum.HIDE.getCode())
                ? BlogAccessMode.RESTRICTED.getCode()
                : BlogAccessMode.PUBLIC.getCode();
        BlogEntity identity =
            blogs.save(
                new BlogEntity(
                    blog.id(),
                    blog.userId(),
                    blog.created(),
                    blog.readCount(),
                    null,
                    null,
                    null,
                    accessMode,
                    blog.eventRevision() == null ? 0L : blog.eventRevision(),
                    blog.eventRevision() == null ? 0L : blog.eventRevision()));
        BlogRevisionEntity revision =
            revisions.save(
                new BlogRevisionEntity(
                    null,
                    blog.id(),
                    1L,
                    BlogRevisionOrigin.PUBLICATION,
                    null,
                    blog.title(),
                    blog.description(),
                    blog.content(),
                    blog.link(),
                    BlogRenderMode.of(blog.status()),
                    now,
                    event.operatorUserId() == null ? blog.userId() : event.operatorUserId(),
                    now));
        identity.setPublishedRevisionId(revision.getId());
        identity.setPublishedAt(revision.getFirstPublishedAt());
        identity.setPublicationRevision(Math.max(1L, identity.getPublicationRevision()));
        revisionSensitive.deleteByRevisionId(revision.getId());
        revisionSensitive.saveAll(
            blog.sensitiveContent().stream()
                .distinct()
                .map(
                    span ->
                        new BlogRevisionSensitiveContentEntity(
                            null,
                            revision.getId(),
                            span.startIndex(),
                            span.endIndex(),
                            span.type()))
                .toList());
        enqueue(blogSnapshot(identity, revision, blog.sensitiveContent()), event);
        enqueueAdminRecovery(
            blog, revision, BlogPersistenceMapper.toIdentity(identity), event);
    }

    @Transactional
    @Override
    public void deleteByIds(List<BlogDeletionTarget> deleted, BlogEventContext event) {
        maintenance.requireWritable();
        for (BlogDeletionTarget target : deleted) {
            Blog blog = target.blog();
            BlogIdentity expected = target.identity();
            long publicationRevision =
                (expected.publicationRevision() == null ? 0L : expected.publicationRevision()) + 1;
            long adminRevision =
                (expected.adminRevision() == null ? 0L : expected.adminRevision()) + 1;
            revisionSensitive.deleteByBlogId(blog.id());
            revisions.deleteByBlogId(blog.id());
            draftSensitive.deleteByBlogId(blog.id());
            drafts.deleteByBlogId(blog.id());
            if (blogs.deleteByIdAndRevisions(
                    blog.id(),
                    expected.publicationRevision(),
                    expected.adminRevision())
                != 1) {
                throw conflict(blog.id());
            }
            enqueue(
                new BlogSnapshot(
                    blog.id(),
                    blog.userId(),
                    blog.title(),
                    blog.description(),
                    blog.content(),
                    blog.created(),
                    blog.updated(),
                    blog.status(),
                    blog.link(),
                    blog.readCount(),
                    publicationRevision),
                event);
            enqueueAdminDelete(blog, expected, adminRevision, event);
        }
    }

    @Transactional
    @Override
    public void incrementViews(Long blogId) {
        blogs.setReadCount(blogId);
    }

    private BlogSnapshot blogSnapshot(
        BlogEntity blog,
        BlogRevisionEntity revision,
        List<BlogSensitiveSpan> sensitiveContent) {
        return new BlogSnapshot(
            blog.getId(),
            blog.getUserId(),
            revision.getTitle(),
            revision.getDescription(),
            revision.getContent(),
            blog.getCreated(),
            revision.getFirstPublishedAt(),
            BlogPersistenceMapper.status(blog, revision),
            revision.getLink(),
            blog.getReadCount(),
            blog.getPublicationRevision(),
            revision.getId(),
            blog.getPublishedAt(),
            blog.getOfflineAt(),
            blog.getAccessMode(),
            revision.getRenderMode().getCode(),
            sensitiveContent);
    }

    private void enqueue(BlogSnapshot snapshot, BlogEventContext event) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            snapshot.id(),
            eventId ->
                new BlogChangedMessage(
                    eventId,
                    event.operation().getCode(),
                    snapshot.revision(),
                    event.operatorUserId(),
                    snapshot));
    }

    private void enqueueAdminDelete(
        Blog blog, BlogIdentity identity, long adminRevision, BlogEventContext event) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            blog.id(),
            eventId ->
                new BlogAdminIndexMessage(
                    eventId,
                    "DELETE",
                    new BlogAdminSnapshot(
                        blog.id(),
                        blog.userId(),
                        null,
                        null,
                        null,
                        BlogAccessMode.RESTRICTED.getCode(),
                        identity.publicationRevision(),
                        adminRevision,
                        blog.readCount(),
                        blog.created()),
                    null,
                    null));
    }

    private void enqueueAdminRecovery(
        Blog blog,
        BlogRevisionEntity revision,
        BlogIdentity identity,
        BlogEventContext event) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            identity.id(),
            eventId ->
                new BlogAdminIndexMessage(
                    eventId,
                    "RECOVER",
                    new BlogAdminSnapshot(
                        identity.id(),
                        identity.userId(),
                        identity.publishedRevisionId(),
                        identity.publishedAt(),
                        identity.offlineAt(),
                        identity.accessMode().getCode(),
                        identity.publicationRevision(),
                        identity.adminRevision(),
                        identity.readCount(),
                        identity.created()),
                    null,
                    new BlogRevisionSnapshot(
                        revision.getId(),
                        revision.getBlogId(),
                        revision.getVersionNo(),
                        revision.getOrigin(),
                        revision.getBaseRevisionId(),
                        revision.getTitle(),
                        revision.getDescription(),
                        revision.getContent(),
                        revision.getLink(),
                        revision.getRenderMode(),
                        revision.getFirstPublishedAt(),
                        revision.getCreatedBy(),
                        revision.getCreatedAt(),
                        blog.sensitiveContent())));
    }

    private static BaseException conflict(Long blogId) {
        return new BaseException(CommonErrorCode.CONFLICT, "blog changed: " + blogId);
    }
}
