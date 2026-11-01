package wiki.chiu.micro.search.adapter.out.elasticsearch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

final class BlogDocumentMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private BlogDocumentMapper() {
    }

    static BlogDocument publicDocument(BlogIndexChange change) {
        BlogIndexEntry blog = change.blog();
        if (change.operation() == BlogIndexChange.Operation.REMOVE) {
            return BlogDocument.builder()
                .id(blog.id())
                .revision(change.revision())
                .deleted(true)
                .build();
        }
        return BlogDocument.builder()
            .id(blog.id())
            .userId(blog.userId())
            .title(blog.title())
            .description(blog.description())
            .content(blog.content())
            .link(blog.link())
            .readCount(blog.readCount())
            .status(blog.status())
            .created(toZonedDateTime(blog.created()))
            .updated(toZonedDateTime(blog.updated()))
            .revision(change.revision())
            .deleted(false)
            .publishedRevisionId(blog.publishedRevisionId())
            .versionNo(blog.versionNo())
            .accessMode(blog.accessMode())
            .renderMode(blog.renderMode())
            .publishedAt(toZonedDateTime(blog.publishedAt()))
            .offlineAt(toZonedDateTime(blog.offlineAt()))
            .sensitiveContent(blog.sensitiveContent())
            .build();
    }

    static AdminBlogDocument adminDocument(BlogAdminIndexChange message) {
        AdminBlogDocument document = new AdminBlogDocument();
        document.apply(adminProjection(null, message));
        return document;
    }

    static AdminRevisionDocument revisionDocument(BlogRevisionSnapshot revision) {
        AdminRevisionDocument document = new AdminRevisionDocument();
        document.apply(
            revision.id(),
            revision.blogId(),
            revision.versionNo(),
            revision.origin(),
            revision.baseRevisionId(),
            revision.title(),
            revision.description(),
            revision.content(),
            revision.link(),
            revision.renderMode(),
            toZonedDateTime(revision.firstPublishedAt()),
            revision.createdBy(),
            toZonedDateTime(revision.createdAt()));
        return document;
    }

    static AdminBlogProjection adminProjection(
        AdminBlogDocument current, BlogAdminIndexChange message) {
        var blog = message.blog();
        var draft = message.draft();
        var revision = message.revision();
        var publishedRevision = message.publishedRevision();
        Long publishedRevisionId =
            blog.publishedRevisionId() != null
                ? blog.publishedRevisionId()
                : current == null ? null : current.getPublishedRevisionId();
        boolean revisionIsPublished =
            revision != null && Objects.equals(revision.id(), publishedRevisionId);
        BlogRevisionSnapshot publishedSource =
            publishedRevision != null
                    && Objects.equals(publishedRevision.id(), publishedRevisionId)
                ? publishedRevision
                : revisionIsPublished ? revision : null;
        String publishedTitle =
            publishedSource != null
                ? publishedSource.title()
                : existing(current, AdminBlogDocument::getPublishedTitle);
        String publishedDescription =
            publishedSource != null
                ? publishedSource.description()
                : existing(current, AdminBlogDocument::getPublishedDescription);
        String publishedContent =
            publishedSource != null
                ? publishedSource.content()
                : existing(current, AdminBlogDocument::getPublishedContent);
        String publishedLink =
            publishedSource != null
                ? publishedSource.link()
                : existing(current, AdminBlogDocument::getPublishedLink);
        BlogRenderMode renderMode =
            publishedSource != null
                ? publishedSource.renderMode()
                : existing(current, AdminBlogDocument::getRenderMode);
        ZonedDateTime publishedAt = toZonedDateTime(blog.publishedAt());
        ZonedDateTime offlineAt = toZonedDateTime(blog.offlineAt());
        boolean hasDraft = draft != null;
        boolean draftNewer =
            draft != null
                && Objects.equals(draft.basePublishedRevisionId(), publishedRevisionId)
                && publishedAt != null
                && draft.updatedAt().isAfter(blog.publishedAt());
        Integer status =
            publishedRevisionId == null
                ? BlogStatusEnum.DRAFT.getCode()
                : offlineAt != null || Objects.equals(blog.accessMode(), 1)
                    ? BlogStatusEnum.HIDE.getCode()
                    : renderMode == BlogRenderMode.SENSITIVE_FILTER
                        ? BlogStatusEnum.SENSITIVE_FILTER.getCode()
                        : BlogStatusEnum.NORMAL.getCode();
        return new AdminBlogProjection(
            blog.id(),
            blog.userId(),
            publishedRevisionId,
            blog.publicationRevision(),
            blog.adminRevision(),
            blog.readCount(),
            toZonedDateTime(blog.createdAt()),
            publishedAt,
            offlineAt,
            blog.accessMode(),
            renderMode,
            draftValue(draft, BlogDraftSnapshot::draftRevision),
            draftValue(draft, BlogDraftSnapshot::basePublishedRevisionId),
            hasDraft,
            draftNewer,
            draft == null ? null : toZonedDateTime(draft.updatedAt()),
            publishedTitle,
            publishedDescription,
            publishedContent,
            publishedLink,
            draftValue(draft, BlogDraftSnapshot::title),
            draftValue(draft, BlogDraftSnapshot::description),
            draftValue(draft, BlogDraftSnapshot::content),
            draftValue(draft, BlogDraftSnapshot::link),
            status,
            "DELETE".equals(message.operation()));
    }

    static PublicBlogRead publicRead(BlogDocument document) {
        return new PublicBlogRead(
            document.getId(),
            document.getUserId(),
            document.getPublishedRevisionId(),
            document.getVersionNo(),
            document.getTitle(),
            document.getDescription(),
            document.getContent(),
            document.getLink(),
            document.getReadCount(),
            toLocalDateTime(document.getCreated()),
            toLocalDateTime(document.getPublishedAt()),
            toLocalDateTime(document.getOfflineAt()),
            document.getAccessMode(),
            document.getRenderMode(),
            document.getSensitiveContent() == null
                ? List.of()
                : document.getSensitiveContent());
    }

    static AdminBlogRead adminRead(AdminBlogDocument document, boolean draftMode) {
        return new AdminBlogRead(
            document.getId(),
            document.getUserId(),
            document.getPublishedRevisionId(),
            document.getPublicationRevision(),
            document.getAdminRevision(),
            draftMode ? document.getDraftTitle() : document.getPublishedTitle(),
            draftMode ? document.getDraftDescription() : document.getPublishedDescription(),
            draftMode ? document.getDraftContent() : document.getPublishedContent(),
            draftMode ? document.getDraftLink() : document.getPublishedLink(),
            document.getReadCount(),
            toLocalDateTime(document.getCreated()),
            Boolean.TRUE.equals(document.getDraftNewer())
                    && document.getDraftUpdatedAt() != null
                ? toLocalDateTime(document.getDraftUpdatedAt())
                : document.getPublishedAt() == null
                    ? toLocalDateTime(document.getCreated())
                    : toLocalDateTime(document.getPublishedAt()),
            document.getStatus(),
            document.getAccessMode(),
            document.getRenderMode(),
            Boolean.TRUE.equals(document.getHasDraft()),
            Boolean.TRUE.equals(document.getDraftNewer()),
            document.getDraftRevision(),
            document.getBasePublishedRevisionId(),
            toLocalDateTime(document.getDraftUpdatedAt()));
    }

    static AdminRevisionRead revisionRead(
        AdminRevisionDocument document, Long currentPublishedRevisionId) {
        return new AdminRevisionRead(
            document.getId(),
            document.getBlogId(),
            document.getVersionNo(),
            document.getOrigin(),
            document.getBaseRevisionId(),
            document.getTitle(),
            toLocalDateTime(document.getFirstPublishedAt()),
            document.getCreatedBy(),
            toLocalDateTime(document.getCreatedAt()),
            document.getId().equals(currentPublishedRevisionId));
    }

    private static ZonedDateTime toZonedDateTime(LocalDateTime value) {
        return value == null ? null : ZonedDateTime.of(value, ZONE_ID);
    }

    private static LocalDateTime toLocalDateTime(ZonedDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private static <T> T existing(
        AdminBlogDocument document, Function<AdminBlogDocument, T> getter) {
        return document == null ? null : getter.apply(document);
    }

    private static <T> T draftValue(
        BlogDraftSnapshot draft, Function<BlogDraftSnapshot, T> getter) {
        return draft == null ? null : getter.apply(draft);
    }

}
