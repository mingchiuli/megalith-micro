package wiki.chiu.micro.blog.application.service;

import java.util.ArrayList;
import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.model.BlogDraft;
import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogPermissions;
import wiki.chiu.micro.blog.application.model.DeletedBlogEntry;
import wiki.chiu.micro.blog.application.model.DeletedBlogItem;
import wiki.chiu.micro.blog.application.model.DeletedBlogPage;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.model.SensitiveContentDraft;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public final class BlogModelConvertor {

    private BlogModelConvertor() {
    }

    public static Blog draftBlog(BlogIdentity identity, BlogWorkingDraft draft) {
        if (draft == null) {
            return new Blog(
                identity.id(),
                identity.userId(),
                "",
                "",
                "",
                identity.created(),
                identity.created(),
                BlogStatusEnum.DRAFT.getCode(),
                "",
                identity.readCount(),
                identity.publicationRevision());
        }
        return new Blog(
            identity.id(),
            identity.userId(),
            draft.title(),
            draft.description(),
            draft.content(),
            identity.created(),
            draft.updatedAt(),
            draft.renderMode() == BlogRenderMode.SENSITIVE_FILTER
                ? BlogStatusEnum.SENSITIVE_FILTER.getCode()
                : BlogStatusEnum.DRAFT.getCode(),
            draft.link(),
            identity.readCount(),
            identity.publicationRevision());
    }

    public static BlogEdit edit(
        Blog blog,
        BlogWorkingDraft draft,
        BlogIdentity identity,
        Long publishedRevisionId,
        List<BlogSensitiveSpan> spans,
        BlogPermissions permissions) {
        boolean draftNewer = draft != null && identity != null && draft.newerThan(identity);
        return new BlogEdit(
            blog.id(),
            blog.userId(),
            blog.title(),
            blog.description(),
            blog.link(),
            blog.content(),
            blog.status(),
            draft == null ? null : draft.draftRevision(),
            draft == null ? null : draft.basePublishedRevisionId(),
            publishedRevisionId,
            draft != null,
            draftNewer,
            spans.stream()
                .map(
                    span ->
                        new SensitiveContentDraft(
                            span.type(), span.startIndex(), span.endIndex()))
                .toList(),
            permissions, identity == null ? null : identity.collaborationEpoch());
    }

    public static BlogContentCommand command(BlogDraft blog) {
        BlogRenderMode renderMode =
            blog.status() != null
                    && blog.status()
                        == BlogStatusEnum.SENSITIVE_FILTER.getCode()
                ? BlogRenderMode.SENSITIVE_FILTER
                : BlogRenderMode.NORMAL;
        return new BlogContentCommand(
            blog.title(),
            blog.description(),
            blog.content(),
            blog.link(),
            renderMode,
            blog.sensitiveContentList().stream()
                .map(
                    span ->
                        new BlogSensitiveSpan(
                            span.startIndex(), span.endIndex(), span.type()))
                .toList());
    }

    public static BlogListItem withRecentReadCount(BlogListItem item, int recentReadCount) {
        return new BlogListItem(
            item.id(),
            item.title(),
            item.description(),
            item.content(),
            item.link(),
            item.readCount(),
            recentReadCount,
            item.created(),
            item.updated(),
            item.status(),
            item.hasDraft(),
            item.draftNewer());
    }

    public static Page<DeletedBlogItem> toDeletedPage(DeletedBlogPage deleted, int currentPage, int size) {
        int totalPages = (int) (deleted.total() % size == 0 ? deleted.total() / size : deleted.total() / size + 1);
        List<DeletedBlogItem> content = new ArrayList<>();
        for (DeletedBlogEntry entry : deleted.entries()) {
            Blog item = entry.blog();
            content.add(
                new DeletedBlogItem(
                    item.id(),
                    item.userId(),
                    item.title(),
                    item.description(),
                    item.content(),
                    item.created(),
                    item.updated(),
                    item.status(),
                    entry.recoveryId(),
                    item.link(),
                    item.readCount()));
        }

        return new Page<>(
            content,
            deleted.total(),
            currentPage,
            size,
            currentPage == 1,
            deleted.total() == 0 || currentPage >= totalPages,
            content.isEmpty(),
            totalPages);
    }

    public static Blog toEditableDraft(BlogIdentity identity, BlogWorkingDraft draft) {
        return new Blog(
            identity.id(),
            identity.userId(),
            draft.title(),
            draft.description(),
            draft.content(),
            identity.created(),
            draft.updatedAt(),
            BlogRenderMode.SENSITIVE_FILTER == draft.renderMode()
                ? 2
                : 0,
            draft.link(),
            identity.readCount(),
            identity.publicationRevision());
    }
}
