package wiki.chiu.micro.blog.adapter.out.persistence.convertor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionSensitiveContentEntity;
import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogPublishCommand;
import wiki.chiu.micro.blog.application.model.BlogVersionWriteCommand;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;

/**
 * Translates persistence entities into framework-free projections.
 */
public final class BlogPersistenceConvertor {

    private BlogPersistenceConvertor() {
    }

    public static BlogIdentity toIdentity(BlogEntity entity) {
        return new BlogIdentity(
            entity.getId(),
            entity.getUserId(),
            entity.getCreated(),
            entity.getReadCount(),
            entity.getPublishedRevisionId(),
            entity.getPublishedAt(),
            entity.getOfflineAt(),
            BlogAccessMode.of(entity.getAccessMode()),
            entity.getPublicationRevision(),
            entity.getAdminRevision(), entity.getCollaborationEpoch(), entity.getHistoryGeneration());
    }

    public static Blog toPublishedBlog(
        BlogEntity entity, BlogRevisionEntity revision, List<BlogSensitiveSpan> spans) {
        if (entity == null || revision == null) {
            return null;
        }
        return new Blog(
            entity.getId(),
            entity.getUserId(),
            revision.getTitle(),
            revision.getDescription(),
            revision.getContent(),
            entity.getCreated(),
            revision.getFirstPublishedAt() == null
                ? revision.getCreatedAt()
                : revision.getFirstPublishedAt(),
            status(entity, revision),
            revision.getLink(),
            entity.getReadCount(),
            entity.getPublicationRevision(),
            spans);
    }

    public static Integer status(BlogEntity blog, BlogRevisionEntity revision) {
        if (revision == null) {
            return BlogStatusEnum.DRAFT.getCode();
        }
        if (blog.getOfflineAt() != null
            || BlogAccessMode.of(blog.getAccessMode()) == BlogAccessMode.RESTRICTED) {
            return BlogStatusEnum.HIDE.getCode();
        }
        return revision.getRenderMode().getCode() == BlogStatusEnum.SENSITIVE_FILTER.getCode()
            ? BlogStatusEnum.SENSITIVE_FILTER.getCode()
            : BlogStatusEnum.NORMAL.getCode();
    }

    public static BlogVersion toVersion(
        BlogRevisionEntity entity, List<BlogSensitiveSpan> spans) {
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
            List.copyOf(spans));
    }

    public static BlogWorkingDraft toDraft(
        BlogDraftEntity entity, List<BlogSensitiveSpan> spans) {
        return new BlogWorkingDraft(
            entity.getId(),
            entity.getBlogId(),
            entity.getDraftRevision(),
            entity.getBasePublishedRevisionId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getContent(),
            entity.getLink(),
            entity.getRenderMode(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt(),
            List.copyOf(spans));
    }

    public static BlogSensitiveSpan toSpan(BlogRevisionSensitiveContentEntity entity) {
        return new BlogSensitiveSpan(
            entity.getStartIndex(), entity.getEndIndex(), entity.getType());
    }

    public static BlogSensitiveSpan toSpan(BlogDraftSensitiveContentEntity entity) {
        return new BlogSensitiveSpan(
            entity.getStartIndex(), entity.getEndIndex(), entity.getType());
    }

    public static List<BlogVersion> toVersions(
        List<BlogRevisionEntity> entities,
        List<BlogRevisionSensitiveContentEntity> sensitive) {
        Map<Long, List<BlogSensitiveSpan>> spansByRevision = sensitive.stream()
            .collect(Collectors.groupingBy(BlogRevisionSensitiveContentEntity::getRevisionId,
                Collectors.mapping(BlogPersistenceConvertor::toSpan, Collectors.toList())));
        return entities.stream()
            .map(entity -> toVersion(entity, spansByRevision.getOrDefault(entity.getId(), List.of())))
            .toList();
    }

    public static BlogSnapshot snapshot(
        BlogEntity blog,
        BlogRevisionEntity revision,
        List<BlogSensitiveSpan> sensitive) {
        return new BlogSnapshot(
            blog.getId(),
            blog.getUserId(),
            revision.getTitle(),
            revision.getDescription(),
            revision.getContent(),
            blog.getCreated(),
            revision.getFirstPublishedAt(),
            status(blog, revision),
            revision.getLink(),
            blog.getReadCount(),
            blog.getPublicationRevision(),
            revision.getId(),
            blog.getPublishedAt(),
            blog.getOfflineAt(),
            blog.getAccessMode(),
            revision.getRenderMode().getCode(),
            sensitive);
    }

    public static BlogAdminSnapshot adminSnapshot(BlogEntity blog) {
        return new BlogAdminSnapshot(
            blog.getId(),
            blog.getUserId(),
            blog.getPublishedRevisionId(),
            blog.getPublishedAt(),
            blog.getOfflineAt(),
            blog.getAccessMode(),
            blog.getPublicationRevision(),
            blog.getAdminRevision(),
            blog.getReadCount(),
            blog.getCreated(), blog.getHistoryGeneration());
    }

    public static BlogDraftSnapshot draftSnapshot(
        BlogDraftEntity draft, List<BlogSensitiveSpan> spans) {
        if (draft == null) {
            return null;
        }
        return new BlogDraftSnapshot(
            draft.getBlogId(),
            draft.getDraftRevision(),
            draft.getBasePublishedRevisionId(),
            draft.getTitle(),
            draft.getDescription(),
            draft.getContent(),
            draft.getLink(),
            draft.getRenderMode(),
            draft.getUpdatedBy(),
            draft.getUpdatedAt(),
            spans);
    }

    public static BlogRevisionSnapshot revisionSnapshot(
        BlogRevisionEntity revision, List<BlogSensitiveSpan> spans, long historyGeneration) {
        if (revision == null) {
            return null;
        }
        return new BlogRevisionSnapshot(
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
            spans, historyGeneration);
    }

    public static Blog draftProjection(BlogEntity blog, BlogDraftEntity draft) {
        return new Blog(
            blog.getId(),
            blog.getUserId(),
            draft.getTitle(),
            draft.getDescription(),
            draft.getContent(),
            blog.getCreated(),
            draft.getUpdatedAt(),
            BlogStatusEnum.DRAFT.getCode(),
            draft.getLink(),
            blog.getReadCount(),
            blog.getAdminRevision());
    }

    public static BlogWorkingDraft draftResult(
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

    public static BlogAdminSnapshot adminSnapshot(BlogIdentity identity) {
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
            identity.created(), identity.historyGeneration());
    }

    public static BlogDraftSnapshot draftSnapshot(BlogWorkingDraft draft) {
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

    public static BlogRevisionSnapshot revisionSnapshot(BlogVersion version, long historyGeneration) {
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
            version.sensitiveContent(), historyGeneration);
    }

    public static BlogVersion withFirstPublishedAt(
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

    public static BlogSnapshot snapshot(BlogIdentity identity, BlogVersion revision) {
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

    public static BlogIdentity withAdminRevision(
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
            adminRevision, identity.collaborationEpoch(), identity.historyGeneration());
    }

    public static BlogRevisionEntity toDraftRevisionEntity(BlogVersionWriteCommand command) {
        return new BlogRevisionEntity(
            null,
            command.identity().id(),
            command.nextVersion(),
            BlogRevisionOrigin.DRAFT,
            command.identity().publishedRevisionId(),
            command.content().title(),
            command.content().description(),
            command.content().content(),
            command.content().link(),
            command.content().renderMode(),
            null,
            command.userId(),
            command.now());
    }

    public static BlogRevisionEntity toArchivedRevisionEntity(BlogPublishCommand command) {
        return new BlogRevisionEntity(
            null,
            command.identity().id(),
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
            command.now());
    }

    public static BlogDraftSensitiveContentEntity toDraftSpanEntity(Long blogId, BlogSensitiveSpan span) {
        return new BlogDraftSensitiveContentEntity(
            null,
            blogId,
            span.startIndex(),
            span.endIndex(),
            span.type());
    }

    public static BlogRevisionSensitiveContentEntity toRevisionSpanEntity(
        Long revisionId,
        BlogSensitiveSpan span) {
        return new BlogRevisionSensitiveContentEntity(
            null,
            revisionId,
            span.startIndex(),
            span.endIndex(),
            span.type());
    }

    public static BlogAdminIndexMessage toAdminMessage(
        String eventId,
        String operation,
        BlogIdentity identity,
        BlogWorkingDraft draft,
        BlogVersion revision,
        BlogVersion publishedRevision) {
        return new BlogAdminIndexMessage(
            eventId,
            operation,
            adminSnapshot(identity),
            draftSnapshot(draft),
            revisionSnapshot(revision, identity.historyGeneration()),
            revisionSnapshot(publishedRevision, identity.historyGeneration()));
    }

    public static BlogChangedMessage toPublicMessage(
        String eventId,
        BlogOperateEnum operation,
        BlogSnapshot snapshot,
        Long userId,
        Long adminRevision,
        Long previousPublishedRevisionId) {
        return new BlogChangedMessage(
            eventId,
            operation.getCode(),
            snapshot.revision(),
            userId,
            snapshot,
            adminRevision,
            previousPublishedRevisionId);
    }

    public static BlogIdentity toPublishedIdentity(
        BlogIdentity original,
        Long revisionId,
        LocalDateTime now,
        int accessMode,
        String nextEpoch) {
        return new BlogIdentity(
            original.id(),
            original.userId(),
            original.created(),
            original.readCount(),
            revisionId,
            now,
            null,
            BlogAccessMode.of(accessMode),
            safeRevision(original.publicationRevision()) + 1,
            safeRevision(original.adminRevision()) + 1, nextEpoch, original.historyGeneration());
    }

    public static BlogIdentity toStatusIdentity(
        BlogIdentity original,
        LocalDateTime publishedAt,
        LocalDateTime offlineAt) {
        return new BlogIdentity(
            original.id(),
            original.userId(),
            original.created(),
            original.readCount(),
            original.publishedRevisionId(),
            publishedAt,
            offlineAt,
            original.accessMode(),
            safeRevision(original.publicationRevision()) + 1,
            safeRevision(original.adminRevision()) + 1, original.collaborationEpoch(), original.historyGeneration());
    }

    public static BlogEntity toRecoveredEntity(Blog blog, int accessMode, long recoveredAdminRevision) {
        return new BlogEntity(
            blog.id(), blog.userId(), blog.created(), blog.readCount(), null, null, null,
            accessMode, safeRevision(blog.eventRevision()), recoveredAdminRevision);
    }

    public static BlogRevisionEntity toRecoveredRevisionEntity(
        Blog blog,
        BlogEventContext event,
        LocalDateTime now) {
        return new BlogRevisionEntity(
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
            now);
    }

    public static BlogSnapshot toDeletedSnapshot(Blog blog, BlogIdentity expected, long publicationRevision) {
        return new BlogSnapshot(
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
            publicationRevision,
            expected.publishedRevisionId(),
            expected.publishedAt(),
            expected.offlineAt(),
            expected.accessMode().getCode(),
            null,
            blog.sensitiveContent());
    }

    public static BlogChangedMessage toChangedMessage(
        String eventId,
        BlogEventContext event,
        BlogSnapshot snapshot,
        Long adminRevision) {
        return new BlogChangedMessage(
            eventId,
            event.operation().getCode(),
            snapshot.revision(),
            event.operatorUserId(),
            snapshot,
            adminRevision);
    }

    public static BlogAdminIndexMessage toAdminDeleteMessage(
        String eventId,
        Blog blog,
        BlogIdentity identity,
        long adminRevision) {
        return new BlogAdminIndexMessage(
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
                blog.created(), identity.historyGeneration()),
            null,
            null,
            null);
    }

    public static BlogAdminIndexMessage toAdminRecoveryMessage(
        String eventId,
        Blog blog,
        BlogRevisionEntity revision,
        BlogIdentity identity) {
        return new BlogAdminIndexMessage(
            eventId,
            "RECOVER",
            adminSnapshot(identity),
            null,
            revisionSnapshot(revision, blog.sensitiveContent(), identity.historyGeneration()),
            revisionSnapshot(revision, blog.sensitiveContent(), identity.historyGeneration()));
    }

    public static BlogDeletionTarget toDeletionTarget(
        BlogEntity entity, BlogRevisionEntity revision, BlogDraftEntity draft, List<BlogSensitiveSpan> spans) {
        Blog blog;
        if (revision != null) {
            blog = toPublishedBlog(entity, revision, spans);
        } else if (draft != null) {
            blog = draftProjection(entity, draft);
        } else {
            blog = new Blog(
                entity.getId(), entity.getUserId(), "", "", "", entity.getCreated(),
                entity.getCreated(), BlogStatusEnum.DRAFT.getCode(), "", entity.getReadCount(),
                entity.getAdminRevision());
        }
        BlogIdentity identity = toIdentity(entity);
        return new BlogDeletionTarget(
            blog,
            new BlogIdentity(
                identity.id(), identity.userId(), identity.created(), identity.readCount(),
                identity.publishedRevisionId(), identity.publishedAt(), identity.offlineAt(),
                identity.accessMode(), safeRevision(identity.publicationRevision()),
                safeRevision(identity.adminRevision()), identity.collaborationEpoch(), identity.historyGeneration()));
    }

    public static BlogAdminIndexSnapshot toAdminIndexSnapshot(
        BlogEntity blog,
        BlogDraftEntity draft,
        List<BlogSensitiveSpan> draftSpans,
        BlogRevisionSnapshot publishedRevision) {
        return new BlogAdminIndexSnapshot(
            adminSnapshot(blog),
            draftSnapshot(
                draft,
                draftSpans),
            publishedRevision,
            publishedRevision);
    }

    private static long safeRevision(Long value) {
        return value == null ? 0L : value;
    }
}
