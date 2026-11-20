package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

final class BlogDocumentConvertor {

    private BlogDocumentConvertor() {
    }

    static BlogDocument publicDocument(BlogIndexChange change) {
        BlogIndexEntry blog = change.blog();
        boolean deleted = change.operation() == BlogIndexChange.Operation.REMOVE;
        return new BlogDocument(
            blog.id(),
            deleted ? null : blog.userId(),
            deleted ? null : blog.status(),
            deleted ? null : blog.readCount(),
            change.revision(),
            deleted ? null : blog.publishedRevisionId(),
            deleted ? null : blog.versionNo(),
            deleted ? null : blog.accessMode(),
            deleted ? null : blog.renderMode(),
            deleted ? null : blog.publishedAt(),
            deleted ? null : blog.offlineAt(),
            deleted ? null : blog.sensitiveContent(),
            deleted,
            deleted ? null : blog.title(),
            deleted ? null : blog.description(),
            deleted ? null : blog.content(),
            deleted ? null : blog.link(),
            deleted ? null : blog.created(),
            deleted ? null : blog.updated());
    }

    static AdminBlogDocument adminDocument(BlogAdminIndexChange message) {
        return adminProjection(null, message);
    }

    static AdminRevisionDocument revisionDocument(BlogRevisionSnapshot revision) {
        return new AdminRevisionDocument(
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
            revision.firstPublishedAt(),
            revision.createdBy(),
            revision.createdAt());
    }

    static AdminBlogDocument adminProjection(
        AdminBlogDocument current, BlogAdminIndexChange message) {
        var blog = message.blog();
        var draft = message.draft();
        var revision = message.revision();
        var publishedRevision = message.publishedRevision();
        Long publishedRevisionId =
            blog.publishedRevisionId() != null
                ? blog.publishedRevisionId()
                : current == null ? null : current.publishedRevisionId();
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
                : existing(current, AdminBlogDocument::publishedTitle);
        String publishedDescription =
            publishedSource != null
                ? publishedSource.description()
                : existing(current, AdminBlogDocument::publishedDescription);
        String publishedContent =
            publishedSource != null
                ? publishedSource.content()
                : existing(current, AdminBlogDocument::publishedContent);
        String publishedLink =
            publishedSource != null
                ? publishedSource.link()
                : existing(current, AdminBlogDocument::publishedLink);
        BlogRenderMode renderMode =
            publishedSource != null
                ? publishedSource.renderMode()
                : existing(current, AdminBlogDocument::renderMode);
        LocalDateTime publishedAt = blog.publishedAt();
        LocalDateTime offlineAt = blog.offlineAt();
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
        return new AdminBlogDocument(
            blog.id(),
            blog.userId(),
            publishedRevisionId,
            blog.publicationRevision(),
            blog.adminRevision(),
            blog.readCount(),
            blog.createdAt(),
            publishedAt,
            offlineAt,
            blog.accessMode(),
            renderMode,
            draftValue(draft, BlogDraftSnapshot::draftRevision),
            draftValue(draft, BlogDraftSnapshot::basePublishedRevisionId),
            hasDraft,
            draftNewer,
            draft == null ? null : draft.updatedAt(),
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
            document.id(),
            document.userId(),
            document.publishedRevisionId(),
            document.versionNo(),
            document.title(),
            document.description(),
            document.content(),
            document.link(),
            document.readCount(),
            document.created(),
            document.publishedAt(),
            document.offlineAt(),
            document.accessMode(),
            document.renderMode(),
            document.sensitiveContent() == null
                ? List.of()
                : document.sensitiveContent());
    }

    static AdminBlogRead adminRead(AdminBlogDocument document, boolean draftMode) {
        return new AdminBlogRead(
            document.id(),
            document.userId(),
            document.publishedRevisionId(),
            document.publicationRevision(),
            document.adminRevision(),
            draftMode ? document.draftTitle() : document.publishedTitle(),
            draftMode ? document.draftDescription() : document.publishedDescription(),
            draftMode ? document.draftContent() : document.publishedContent(),
            draftMode ? document.draftLink() : document.publishedLink(),
            document.readCount(),
            document.created(),
            Boolean.TRUE.equals(document.draftNewer())
                    && document.draftUpdatedAt() != null
                ? document.draftUpdatedAt()
                : document.publishedAt() == null
                    ? document.created()
                    : document.publishedAt(),
            document.status(),
            document.accessMode(),
            document.renderMode(),
            Boolean.TRUE.equals(document.hasDraft()),
            Boolean.TRUE.equals(document.draftNewer()),
            document.draftRevision(),
            document.basePublishedRevisionId(),
            document.draftUpdatedAt());
    }

    static AdminRevisionRead revisionRead(
        AdminRevisionDocument document, Long currentPublishedRevisionId) {
        return new AdminRevisionRead(
            document.id(),
            document.blogId(),
            document.versionNo(),
            document.origin(),
            document.baseRevisionId(),
            document.title(),
            document.firstPublishedAt(),
            document.createdBy(),
            document.createdAt(),
            document.id().equals(currentPublishedRevisionId));
    }

    private static <T> T existing(
        AdminBlogDocument document, Function<AdminBlogDocument, T> getter) {
        return document == null ? null : getter.apply(document);
    }

    private static <T> T draftValue(
        BlogDraftSnapshot draft, Function<BlogDraftSnapshot, T> getter) {
        return draft == null ? null : getter.apply(draft);
    }

    static BlogRevisionSnapshot toSnapshot(BlogRevisionIndexEntry revision) {
        return new BlogRevisionSnapshot(
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
            revision.firstPublishedAt(),
            revision.createdBy(),
            revision.createdAt(),
            revision.sensitiveContent());
    }

    static BlogSearchHit searchHit(BlogDocument document, float score, Map<String, List<String>> highlights) {
        return new BlogSearchHit(document.id(), document.userId(), document.status(), document.title(),
            document.description(), document.content(), document.link(), document.created(), score, highlights);
    }

    static <T> Page<T> page(List<T> rows, long total, int page, int size) {
        int pages = Math.toIntExact((total + size - 1) / size);
        return new Page<>(rows, total, page, size, page == 1, page >= pages, total == 0, pages);
    }
}
