package wiki.chiu.micro.blog.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
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
                    BlogAccessMode.PUBLIC.getCode(),
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
                    LocalDateTime.now(),
                    event.operatorUserId() == null ? blog.userId() : event.operatorUserId(),
                    LocalDateTime.now()));
        identity.setPublishedRevisionId(revision.getId());
        identity.setPublishedAt(revision.getFirstPublishedAt());
        identity.setPublicationRevision(Math.max(1L, identity.getPublicationRevision()));
        enqueue(blogSnapshot(identity, revision), event);
    }

    @Transactional
    @Override
    public void deleteByIds(
        List<Blog> deleted, List<Long> sensitiveIds, BlogEventContext event) {
        maintenance.requireWritable();
        for (Blog blog : deleted) {
            revisionSensitive.deleteByBlogId(blog.id());
            revisions.deleteByBlogId(blog.id());
            draftSensitive.deleteByBlogId(blog.id());
            drafts.deleteByBlogId(blog.id());
            blogs.deleteById(blog.id());
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
                    blog.eventRevision()),
                event);
            enqueueAdminDelete(blog, event);
        }
    }

    @Transactional
    @Override
    public void incrementViews(Long blogId) {
        blogs.setReadCount(blogId);
    }

    private BlogSnapshot blogSnapshot(BlogEntity blog, BlogRevisionEntity revision) {
        return new BlogSnapshot(
            blog.getId(),
            blog.getUserId(),
            revision.getTitle(),
            revision.getDescription(),
            revision.getContent(),
            blog.getCreated(),
            revision.getFirstPublishedAt(),
            revision.getRenderMode().getCode(),
            revision.getLink(),
            blog.getReadCount(),
            blog.getPublicationRevision(),
            revision.getId(),
            blog.getPublishedAt(),
            blog.getOfflineAt(),
            blog.getAccessMode(),
            revision.getRenderMode().getCode(),
            List.of());
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

    private void enqueueAdminDelete(Blog blog, BlogEventContext event) {
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
                        blog.updated(),
                        blog.updated(),
                        BlogAccessMode.RESTRICTED.getCode(),
                        blog.eventRevision(),
                        blog.eventRevision(),
                        blog.readCount(),
                        blog.created()),
                    null,
                    null));
    }
}
