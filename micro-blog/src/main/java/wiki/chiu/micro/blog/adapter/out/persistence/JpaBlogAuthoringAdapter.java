package wiki.chiu.micro.blog.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.blog.adapter.out.persistence.convertor.BlogPersistenceConvertor;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.model.BlogDraftDeleteCommand;
import wiki.chiu.micro.blog.application.model.BlogDraftWriteCommand;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.application.model.BlogPublishCommand;
import wiki.chiu.micro.blog.application.model.BlogStatusWriteCommand;
import wiki.chiu.micro.blog.application.model.BlogVersionWriteCommand;
import wiki.chiu.micro.blog.application.model.SavedBlogVersion;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringWriter;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

@Component
public class JpaBlogAuthoringAdapter implements BlogAuthoringWriter {

    private final BlogRepository blogs;
    private final BlogRevisionRepository revisions;
    private final BlogDraftRepository drafts;
    private final BlogRevisionSensitiveContentRepository revisionSensitive;
    private final BlogDraftSensitiveContentRepository draftSensitive;
    private final OutboxEvents outbox;
    private final BlogMaintenanceMode maintenance;

    public JpaBlogAuthoringAdapter(
        BlogRepository blogs,
        BlogRevisionRepository revisions,
        BlogDraftRepository drafts,
        BlogRevisionSensitiveContentRepository revisionSensitive,
        BlogDraftSensitiveContentRepository draftSensitive,
        OutboxEvents outbox, BlogMaintenanceMode maintenance) {
        this.blogs = blogs;
        this.revisions = revisions;
        this.drafts = drafts;
        this.revisionSensitive = revisionSensitive;
        this.draftSensitive = draftSensitive;
        this.outbox = outbox;
        this.maintenance = maintenance;
    }

    @Transactional
    @Override
    public BlogIdentity createIdentity(Long userId) {
        maintenance.requireWritable();
        BlogEntity entity = blogs.save(BlogEntity.newBlog(userId));
        BlogIdentity identity = BlogPersistenceConvertor.toIdentity(entity);
        enqueueAdmin(identity, null, null, null, "BLOG_CREATED");
        return identity;
    }

    @Transactional
    @Override
    public BlogWorkingDraft saveDraft(BlogDraftWriteCommand command) {
        maintenance.requireWritable();
        BlogIdentity original = command.identity();
        long expectedAdmin = revision(original.adminRevision());
        if (blogs.bumpAdminRevision(original.id(), expectedAdmin, original.collaborationEpoch()) != 1) {
            throw conflict(original.id());
        }

        BlogWorkingDraft current = command.currentDraft();
        long draftRevision = current == null ? 1L : current.draftRevision() + 1;
        if (current == null) {
            BlogDraftEntity entity =
                BlogDraftEntity.create(
                    original.id(),
                    original.publishedRevisionId(),
                    command.content().title(),
                    command.content().description(),
                    command.content().content(),
                    command.content().link(),
                    command.content().renderMode(),
                    command.userId(),
                    command.now());
            drafts.save(entity);
        } else {
            updateDraft(current, original, command.content(), command.userId(), command.now());
        }
        replaceDraftSensitive(original.id(), command.content().sensitiveContent());
        BlogWorkingDraft result =
            BlogPersistenceConvertor.draftResult(
                original, current, draftRevision, command.content(), command.userId(), command.now());
        BlogIdentity changed = BlogPersistenceConvertor.withAdminRevision(original, expectedAdmin + 1);
        enqueueAdmin(changed, result, null, command.publishedRevision(), "DRAFT_UPSERT");
        return result;
    }

    @Transactional
    @Override
    public void deleteDraft(BlogDraftDeleteCommand command) {
        maintenance.requireWritable();
        BlogIdentity original = command.identity();
        long expectedAdmin = revision(original.adminRevision());
        if (blogs.bumpAdminRevision(original.id(), expectedAdmin, original.collaborationEpoch()) != 1) {
            throw conflict(original.id());
        }
        if (blogs.rotateEpoch(original.id(), original.collaborationEpoch(), UUID.randomUUID().toString()) != 1) {
            throw conflict(original.id());
        }
        if (command.draft() != null) {
            drafts.deleteByBlogId(original.id());
        }
        draftSensitive.deleteByBlogId(original.id());
        enqueueAdmin(
            BlogPersistenceConvertor.withAdminRevision(original, expectedAdmin + 1),
            null,
            null,
            command.publishedRevision(),
            "DRAFT_DELETE");
    }

    @Transactional
    @Override
    public SavedBlogVersion saveDraftVersion(BlogVersionWriteCommand command) {
        maintenance.requireWritable();
        BlogIdentity original = command.identity();
        long expectedAdmin = revision(original.adminRevision());
        if (blogs.bumpAdminRevision(original.id(), expectedAdmin, original.collaborationEpoch()) != 1) {
            throw conflict(original.id());
        }

        BlogWorkingDraft current = command.currentDraft();
        long draftRevision = current == null ? 1L : current.draftRevision() + 1;
        if (current == null) {
            drafts.save(
                BlogDraftEntity.create(
                    original.id(),
                    original.publishedRevisionId(),
                    command.content().title(),
                    command.content().description(),
                    command.content().content(),
                    command.content().link(),
                    command.content().renderMode(),
                    command.userId(),
                    command.now()));
        } else {
            updateDraft(current, original, command.content(), command.userId(), command.now());
        }
        replaceDraftSensitive(original.id(), command.content().sensitiveContent());

        BlogRevisionEntity savedRevision =
            revisions.save(
                BlogPersistenceConvertor.toDraftRevisionEntity(command));
        insertRevisionSensitive(savedRevision.getId(), command.content().sensitiveContent());

        BlogWorkingDraft draft =
            BlogPersistenceConvertor.draftResult(
                original, current, draftRevision, command.content(), command.userId(), command.now());
        BlogVersion version =
            BlogPersistenceConvertor.toVersion(savedRevision, command.content().sensitiveContent());
        BlogIdentity changed = BlogPersistenceConvertor.withAdminRevision(original, expectedAdmin + 1);
        enqueueAdmin(
            changed,
            draft,
            version,
            command.publishedRevision(),
            "REVISION_CREATE");
        return new SavedBlogVersion(draft, version);
    }

    @Transactional
    @Override
    public void publish(BlogPublishCommand command) {
        maintenance.requireWritable();
        BlogIdentity original = command.identity();
        BlogVersion selected = command.revision();
        String nextEpoch = UUID.randomUUID().toString();
        int accessMode =
            command.accessMode() == null
                ? original.accessMode().getCode()
                : BlogAccessMode.of(command.accessMode()).getCode();
        if (blogs.publish(
                original.id(),
                selected.id(),
                nextEpoch,
                command.now(),
                command.accessMode(),
                revision(original.publicationRevision()),
                revision(original.adminRevision()), original.collaborationEpoch())
            != 1) {
            throw conflict(original.id());
        }

        if (selected.firstPublishedAt() == null
            && revisions.markFirstPublished(selected.id(), original.id(), command.now()) != 1) {
            throw conflict(original.id());
        }

        BlogVersion archivedVersion = null;
        if (command.currentDraft() != null) {
            if (command.archivedDraft() != null) {
                BlogRevisionEntity archived =
                    revisions.save(
                        BlogPersistenceConvertor.toArchivedRevisionEntity(command));
                insertRevisionSensitive(
                    archived.getId(), command.archivedDraft().sensitiveContent());
                archivedVersion =
                    BlogPersistenceConvertor.toVersion(archived, command.archivedDraft().sensitiveContent());
            }
            drafts.deleteByBlogId(original.id());
            draftSensitive.deleteByBlogId(original.id());
        }

        BlogVersion published =
            selected.firstPublishedAt() == null
                ? BlogPersistenceConvertor.withFirstPublishedAt(selected, command.now())
                : selected;
        BlogIdentity changed =
            BlogPersistenceConvertor.toPublishedIdentity(
                original, selected.id(), command.now(), accessMode, nextEpoch);
        enqueuePublic(
            BlogOperateEnum.UPDATE,
            BlogPersistenceConvertor.snapshot(changed, published),
            original.publishedRevisionId(),
            command.userId(),
            changed.adminRevision());
        enqueueAdmin(
            changed,
            null,
            published,
            published,
            "PUBLICATION_UPSERT");
        if (command.archivedDraft() != null) {
            enqueueAdmin(
                changed,
                null,
                archivedVersion,
                published,
                "REVISION_CREATE");
        }
    }

    @Transactional
    @Override
    public void changeStatus(BlogStatusWriteCommand command) {
        maintenance.requireWritable();
        BlogIdentity original = command.identity();
        int changed =
            command.online()
                ? blogs.setOnlineAt(
                    original.id(),
                    command.now(),
                    revision(original.publicationRevision()),
                    revision(original.adminRevision()), original.collaborationEpoch())
                : blogs.setOfflineAt(
                    original.id(),
                    command.now(),
                    revision(original.publicationRevision()),
                    revision(original.adminRevision()), original.collaborationEpoch());
        if (changed != 1) {
            throw conflict(original.id());
        }

        BlogIdentity next =
            BlogPersistenceConvertor.toStatusIdentity(
                original,
                command.online() ? command.now() : original.publishedAt(),
                command.online() ? null : command.now());
        enqueuePublic(
            BlogOperateEnum.UPDATE,
            BlogPersistenceConvertor.snapshot(next, command.revision()),
            null,
            command.userId(),
            next.adminRevision());
        enqueueAdmin(
            next,
            command.draft(),
            command.revision(),
            command.revision(),
            command.online() ? "ONLINE" : "OFFLINE");
    }

    private void updateDraft(
        BlogWorkingDraft current,
        BlogIdentity identity,
        BlogContentCommand content,
        Long userId,
        LocalDateTime now) {
        if (drafts.updateContent(
                current.id(),
                current.draftRevision(),
                identity.publishedRevisionId(),
                content.title(),
                content.description(),
                content.content(),
                content.link(),
                content.renderMode(),
                userId,
                now)
            != 1) {
            throw conflict(identity.id());
        }
    }

    private void replaceDraftSensitive(
        Long blogId, List<BlogSensitiveSpan> spans) {
        draftSensitive.deleteByBlogId(blogId);
        draftSensitive.saveAll(
            spans.stream()
                .distinct()
                .map(
                    span ->
                        BlogPersistenceConvertor.toDraftSpanEntity(blogId, span))
                .toList());
    }

    private void insertRevisionSensitive(
        Long revisionId, List<BlogSensitiveSpan> spans) {
        revisionSensitive.saveAll(
            spans.stream()
                .distinct()
                .map(
                    span ->
                        BlogPersistenceConvertor.toRevisionSpanEntity(revisionId, span))
                .toList());
    }

    private void enqueueAdmin(
        BlogIdentity identity,
        BlogWorkingDraft draft,
        BlogVersion revision,
        BlogVersion publishedRevision,
        String operation) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            identity.id(),
            eventId ->
                BlogPersistenceConvertor.toAdminMessage(
                    eventId, operation, identity, draft, revision, publishedRevision));
    }

    private void enqueuePublic(
        BlogOperateEnum operation, BlogSnapshot snapshot, Long previousPublishedRevisionId,
        Long userId, Long adminRevision) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            snapshot.id(),
            eventId ->
                BlogPersistenceConvertor.toPublicMessage(
                    eventId, operation, snapshot, userId, adminRevision, previousPublishedRevisionId));
    }

    private static long revision(Long value) {
        return value == null ? 0L : value;
    }

    private static BaseException conflict(Long blogId) {
        return new BaseException(
            CommonErrorCode.CONFLICT, "blog revision conflict: " + blogId);
    }
}
