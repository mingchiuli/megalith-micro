package wiki.chiu.micro.blog.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.model.BlogDraftDeleteCommand;
import wiki.chiu.micro.blog.application.model.BlogDraftWriteCommand;
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
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
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

    public JpaBlogAuthoringAdapter(
        BlogRepository blogs,
        BlogRevisionRepository revisions,
        BlogDraftRepository drafts,
        BlogRevisionSensitiveContentRepository revisionSensitive,
        BlogDraftSensitiveContentRepository draftSensitive,
        OutboxEvents outbox) {
        this.blogs = blogs;
        this.revisions = revisions;
        this.drafts = drafts;
        this.revisionSensitive = revisionSensitive;
        this.draftSensitive = draftSensitive;
        this.outbox = outbox;
    }

    @Transactional
    @Override
    public BlogIdentity createIdentity(Long userId) {
        BlogEntity entity = blogs.save(BlogEntity.newBlog(userId));
        BlogIdentity identity = BlogPersistenceMapper.toIdentity(entity);
        enqueueAdmin(identity, null, null, null, "BLOG_CREATED");
        return identity;
    }

    @Transactional
    @Override
    public BlogWorkingDraft saveDraft(BlogDraftWriteCommand command) {
        BlogIdentity original = command.identity();
        long expectedAdmin = revision(original.adminRevision());
        if (blogs.bumpAdminRevision(original.id(), expectedAdmin) != 1) {
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
            draftResult(original, current, draftRevision, command.content(), command.userId(), command.now());
        BlogIdentity changed = withAdminRevision(original, expectedAdmin + 1);
        enqueueAdmin(changed, result, null, command.publishedRevision(), "DRAFT_UPSERT");
        return result;
    }

    @Transactional
    @Override
    public void deleteDraft(BlogDraftDeleteCommand command) {
        BlogIdentity original = command.identity();
        long expectedAdmin = revision(original.adminRevision());
        if (blogs.bumpAdminRevision(original.id(), expectedAdmin) != 1) {
            throw conflict(original.id());
        }
        if (command.draft() != null) {
            drafts.deleteByBlogId(original.id());
        }
        draftSensitive.deleteByBlogId(original.id());
        enqueueAdmin(
            withAdminRevision(original, expectedAdmin + 1),
            null,
            null,
            command.publishedRevision(),
            "DRAFT_DELETE");
    }

    @Transactional
    @Override
    public SavedBlogVersion saveDraftVersion(BlogVersionWriteCommand command) {
        BlogIdentity original = command.identity();
        long expectedAdmin = revision(original.adminRevision());
        if (blogs.bumpAdminRevision(original.id(), expectedAdmin) != 1) {
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
                new BlogRevisionEntity(
                    null,
                    original.id(),
                    command.nextVersion(),
                    BlogRevisionOrigin.DRAFT,
                    original.publishedRevisionId(),
                    command.content().title(),
                    command.content().description(),
                    command.content().content(),
                    command.content().link(),
                    command.content().renderMode(),
                    null,
                    command.userId(),
                    command.now()));
        replaceRevisionSensitive(savedRevision.getId(), command.content().sensitiveContent());

        BlogWorkingDraft draft =
            draftResult(original, current, draftRevision, command.content(), command.userId(), command.now());
        BlogVersion version =
            version(savedRevision, command.content().sensitiveContent());
        BlogIdentity changed = withAdminRevision(original, expectedAdmin + 1);
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
        BlogIdentity original = command.identity();
        BlogVersion selected = command.revision();
        if (selected.firstPublishedAt() == null
            && revisions.markFirstPublished(selected.id(), original.id(), command.now()) != 1) {
            throw conflict(original.id());
        }
        int accessMode =
            command.accessMode() == null
                ? original.accessMode().getCode()
                : BlogAccessMode.of(command.accessMode()).getCode();
        if (blogs.publish(
                original.id(),
                selected.id(),
                command.now(),
                command.accessMode(),
                revision(original.publicationRevision()),
                revision(original.adminRevision()))
            != 1) {
            throw conflict(original.id());
        }

        BlogVersion archivedVersion = null;
        if (command.currentDraft() != null) {
            if (command.archivedDraft() != null) {
                BlogRevisionEntity archived =
                    revisions.save(
                        new BlogRevisionEntity(
                            null,
                            original.id(),
                            command.archivedVersionNo(),
                            BlogRevisionOrigin.DRAFT,
                            command.archivedDraft().basePublishedRevisionId(),
                            command.archivedDraft().title(),
                            command.archivedDraft().description(),
                            command.archivedDraft().content(),
                            command.archivedDraft().link(),
                            command.archivedDraft().renderMode(),
                            null,
                            command.userId(),
                            command.now()));
                replaceRevisionSensitive(
                    archived.getId(), command.archivedDraft().sensitiveContent());
                archivedVersion =
                    version(archived, command.archivedDraft().sensitiveContent());
            }
            drafts.deleteByBlogId(original.id());
            draftSensitive.deleteByBlogId(original.id());
        }

        BlogVersion published =
            selected.firstPublishedAt() == null
                ? withFirstPublishedAt(selected, command.now())
                : selected;
        BlogIdentity changed =
            new BlogIdentity(
                original.id(),
                original.userId(),
                original.created(),
                original.readCount(),
                selected.id(),
                command.now(),
                null,
                BlogAccessMode.of(accessMode),
                revision(original.publicationRevision()) + 1,
                revision(original.adminRevision()) + 1);
        enqueuePublic(
            BlogOperateEnum.UPDATE,
            snapshot(changed, published),
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
        BlogIdentity original = command.identity();
        int changed =
            command.online()
                ? blogs.setOnlineAt(
                    original.id(),
                    command.now(),
                    revision(original.publicationRevision()),
                    revision(original.adminRevision()))
                : blogs.setOfflineAt(
                    original.id(),
                    command.now(),
                    revision(original.publicationRevision()),
                    revision(original.adminRevision()));
        if (changed != 1) {
            throw conflict(original.id());
        }

        BlogIdentity next =
            new BlogIdentity(
                original.id(),
                original.userId(),
                original.created(),
                original.readCount(),
                original.publishedRevisionId(),
                command.online() ? command.now() : original.publishedAt(),
                command.online() ? null : command.now(),
                original.accessMode(),
                revision(original.publicationRevision()) + 1,
                revision(original.adminRevision()) + 1);
        enqueuePublic(
            BlogOperateEnum.UPDATE,
            snapshot(next, command.revision()),
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

    private BlogWorkingDraft draftResult(
        BlogIdentity identity,
        BlogWorkingDraft current,
        long draftRevision,
        BlogContentCommand content,
        Long userId,
        LocalDateTime now) {
        return new BlogWorkingDraft(
            current == null ? null : current.id(),
            identity.id(),
            draftRevision,
            identity.publishedRevisionId(),
            content.title(),
            content.description(),
            content.content(),
            content.link(),
            content.renderMode(),
            userId,
            now,
            content.sensitiveContent());
    }

    private void replaceDraftSensitive(
        Long blogId, List<BlogSensitiveSpan> spans) {
        draftSensitive.deleteByBlogId(blogId);
        draftSensitive.saveAll(
            spans.stream()
                .distinct()
                .map(
                    span ->
                        new BlogDraftSensitiveContentEntity(
                            null,
                            blogId,
                            span.startIndex(),
                            span.endIndex(),
                            span.type()))
                .toList());
    }

    private void replaceRevisionSensitive(
        Long revisionId, List<BlogSensitiveSpan> spans) {
        revisionSensitive.deleteByRevisionId(revisionId);
        revisionSensitive.saveAll(
            spans.stream()
                .distinct()
                .map(
                    span ->
                        new BlogRevisionSensitiveContentEntity(
                            null,
                            revisionId,
                            span.startIndex(),
                            span.endIndex(),
                            span.type()))
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
                new BlogAdminIndexMessage(
                    eventId,
                    operation,
                    adminSnapshot(identity),
                    draftSnapshot(draft),
                    revisionSnapshot(revision),
                    revisionSnapshot(publishedRevision)));
    }

    private void enqueuePublic(
        BlogOperateEnum operation, BlogSnapshot snapshot, Long userId, Long adminRevision) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            snapshot.id(),
            eventId ->
                new BlogChangedMessage(
                    eventId,
                    operation.getCode(),
                    snapshot.revision(),
                    userId,
                    snapshot,
                    adminRevision));
    }

    private static BlogAdminSnapshot adminSnapshot(BlogIdentity identity) {
        return new BlogAdminSnapshot(
            identity.id(),
            identity.userId(),
            identity.publishedRevisionId(),
            identity.publishedAt(),
            identity.offlineAt(),
            identity.accessMode().getCode(),
            identity.publicationRevision(),
            identity.adminRevision(),
            identity.readCount(),
            identity.created());
    }

    private static BlogDraftSnapshot draftSnapshot(BlogWorkingDraft draft) {
        if (draft == null) {
            return null;
        }
        return new BlogDraftSnapshot(
            draft.blogId(),
            draft.draftRevision(),
            draft.basePublishedRevisionId(),
            draft.title(),
            draft.description(),
            draft.content(),
            draft.link(),
            draft.renderMode(),
            draft.updatedBy(),
            draft.updatedAt(),
            draft.sensitiveContent());
    }

    private static BlogRevisionSnapshot revisionSnapshot(BlogVersion version) {
        if (version == null) {
            return null;
        }
        return new BlogRevisionSnapshot(
            version.id(),
            version.blogId(),
            version.versionNo(),
            version.origin(),
            version.baseRevisionId(),
            version.title(),
            version.description(),
            version.content(),
            version.link(),
            version.renderMode(),
            version.firstPublishedAt(),
            version.createdBy(),
            version.createdAt(),
            version.sensitiveContent());
    }

    private static BlogVersion version(
        BlogRevisionEntity entity, List<BlogSensitiveSpan> sensitiveContent) {
        return new BlogVersion(
            entity.getId(),
            entity.getBlogId(),
            entity.getVersionNo(),
            entity.getOrigin(),
            entity.getBaseRevisionId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getContent(),
            entity.getLink(),
            entity.getRenderMode(),
            entity.getFirstPublishedAt(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            sensitiveContent);
    }

    private static BlogVersion withFirstPublishedAt(
        BlogVersion version, LocalDateTime publishedAt) {
        return new BlogVersion(
            version.id(),
            version.blogId(),
            version.versionNo(),
            version.origin(),
            version.baseRevisionId(),
            version.title(),
            version.description(),
            version.content(),
            version.link(),
            version.renderMode(),
            publishedAt,
            version.createdBy(),
            version.createdAt(),
            version.sensitiveContent());
    }

    private static BlogSnapshot snapshot(BlogIdentity identity, BlogVersion revision) {
        Integer status =
            identity.offlineAt() != null
                    || identity.accessMode() == BlogAccessMode.RESTRICTED
                ? BlogStatusEnum.HIDE.getCode()
                : revision.renderMode() == BlogRenderMode.SENSITIVE_FILTER
                    ? BlogStatusEnum.SENSITIVE_FILTER.getCode()
                    : BlogStatusEnum.NORMAL.getCode();
        return new BlogSnapshot(
            identity.id(),
            identity.userId(),
            revision.title(),
            revision.description(),
            revision.content(),
            identity.created(),
            revision.firstPublishedAt(),
            status,
            revision.link(),
            identity.readCount(),
            identity.publicationRevision(),
            revision.id(),
            identity.publishedAt(),
            identity.offlineAt(),
            identity.accessMode().getCode(),
            revision.renderMode().getCode(),
            revision.sensitiveContent());
    }

    private static BlogIdentity withAdminRevision(
        BlogIdentity identity, long adminRevision) {
        return new BlogIdentity(
            identity.id(),
            identity.userId(),
            identity.created(),
            identity.readCount(),
            identity.publishedRevisionId(),
            identity.publishedAt(),
            identity.offlineAt(),
            identity.accessMode(),
            identity.publicationRevision(),
            adminRevision);
    }

    private static long revision(Long value) {
        return value == null ? 0L : value;
    }

    private static BaseException conflict(Long blogId) {
        return new BaseException(
            CommonErrorCode.CONFLICT, "blog revision conflict: " + blogId);
    }
}
