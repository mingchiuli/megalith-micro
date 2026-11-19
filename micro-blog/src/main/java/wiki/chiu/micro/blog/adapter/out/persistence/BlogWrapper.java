package wiki.chiu.micro.blog.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.blog.adapter.out.persistence.convertor.BlogPersistenceConvertor;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
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
        long recoveredAdminRevision =
            blog.adminRevision() == null
                ? Math.max(System.currentTimeMillis(), safeRevision(blog.eventRevision()) + 1)
                : blog.adminRevision();
        int accessMode =
            Objects.equals(blog.status(), BlogStatusEnum.HIDE.getCode())
                ? BlogAccessMode.RESTRICTED.getCode()
                : BlogAccessMode.PUBLIC.getCode();
        BlogEntity identity = BlogPersistenceConvertor.toRecoveredEntity(blog, accessMode, recoveredAdminRevision);
        try {
            blogs.insertRecovered(
                identity.getId(), identity.getUserId(), identity.getCreated(), identity.getReadCount(),
                identity.getAccessMode(), identity.getPublicationRevision(), identity.getAdminRevision(), identity.getCollaborationEpoch());
        } catch (org.springframework.dao.DataIntegrityViolationException failure) {
            throw new BaseException(CommonErrorCode.CONFLICT, "blog already restored: " + blog.id());
        }

        BlogRevisionEntity revision =
            revisions.save(
                BlogPersistenceConvertor.toRecoveredRevisionEntity(blog, event, now));
        identity.setPublishedRevisionId(revision.getId());
        identity.setPublishedAt(revision.getFirstPublishedAt());
        identity.setPublicationRevision(Math.max(1L, identity.getPublicationRevision()));
        if (blogs.attachRecoveredPublication(
                identity.getId(), revision.getId(), identity.getPublishedAt(),
                identity.getPublicationRevision()) != 1) {
            throw conflict(blog.id());
        }
        revisionSensitive.saveAll(
            blog.sensitiveContent().stream()
                .distinct()
                .map(
                    span ->
                        BlogPersistenceConvertor.toRevisionSpanEntity(revision.getId(), span))
                .toList());
        enqueue(
            BlogPersistenceConvertor.snapshot(identity, revision, blog.sensitiveContent()),
            event,
            recoveredAdminRevision);
        enqueueAdminRecovery(
            blog, revision, BlogPersistenceConvertor.toIdentity(identity), event);
    }

    @Transactional
    @Override
    public void deleteByIds(List<BlogDeletionTarget> deleted, BlogEventContext event) {
        maintenance.requireWritable();
        // Acquire aggregate locks in the same order across overlapping batches.
        for (BlogDeletionTarget target : deleted.stream()
            .sorted(Comparator.comparing(target -> target.identity().id())).toList()) {
            Blog blog = target.blog();
            BlogIdentity expected = target.identity();
            long publicationRevision =
                (expected.publicationRevision() == null ? 0L : expected.publicationRevision()) + 1;
            long adminRevision =
                (expected.adminRevision() == null ? 0L : expected.adminRevision()) + 1;
            if (blogs.reserveDeletion(
                    blog.id(), expected.publicationRevision(), expected.adminRevision(),
                    expected.collaborationEpoch()) != 1) {
                throw conflict(blog.id());
            }
            revisionSensitive.deleteByBlogId(blog.id());
            revisions.deleteByBlogId(blog.id());
            drafts.deleteByBlogId(blog.id());
            draftSensitive.deleteByBlogId(blog.id());
            if (blogs.deleteByIdAndRevisions(
                    blog.id(),
                    expected.publicationRevision(),
                    adminRevision, expected.collaborationEpoch())
                != 1) {
                throw conflict(blog.id());
            }
            enqueue(
                BlogPersistenceConvertor.toDeletedSnapshot(blog, expected, publicationRevision),
                event,
                adminRevision);
            enqueueAdminDelete(blog, expected, adminRevision, event);
        }
    }

    @Transactional
    @Override
    public void incrementViews(Long blogId) {
        blogs.setReadCount(blogId);
    }

    private void enqueue(
        BlogSnapshot snapshot, BlogEventContext event, Long adminRevision) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            snapshot.id(),
            eventId ->
                BlogPersistenceConvertor.toChangedMessage(eventId, event, snapshot, adminRevision));
    }

    private void enqueueAdminDelete(
        Blog blog, BlogIdentity identity, long adminRevision, BlogEventContext event) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            blog.id(),
            eventId ->
                BlogPersistenceConvertor.toAdminDeleteMessage(eventId, blog, identity, adminRevision));
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
                BlogPersistenceConvertor.toAdminRecoveryMessage(eventId, blog, revision, identity));
    }

    private static long safeRevision(Long revision) {
        return revision == null ? 0L : revision;
    }

    private static BaseException conflict(Long blogId) {
        return new BaseException(CommonErrorCode.CONFLICT, "blog changed: " + blogId);
    }
}
