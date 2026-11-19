package wiki.chiu.micro.search.adapter.out.http;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

public final class BlogIndexSourceConvertor {

    private BlogIndexSourceConvertor() {
    }

    public static IndexSourceStatus toStatus(BlogIndexSourceStatus status) {
        return new IndexSourceStatus(status.readOnly(), status.readyEvents(), status.pausedEvents(), status.total(), status.publishedTotal());
    }

    public static BlogIndexChange toChange(BlogSnapshot blog) {
        return new BlogIndexChange(BlogIndexChange.Operation.CREATE, blog.revision(),
            new BlogIndexEntry(
                blog.id(),
                blog.userId(),
                blog.status(),
                blog.readCount(),
                blog.title(),
                blog.description(),
                blog.content(),
                blog.link(),
                blog.created(),
                blog.updated(),
                blog.publishedRevisionId(),
                null,
                blog.publishedAt(),
                blog.offlineAt(),
                blog.accessMode(),
                BlogRenderMode.of(blog.renderMode()),
                blog.sensitiveContent() == null ? List.of() : blog.sensitiveContent()));
    }

    public static BlogAdminIndexChange toAdminChange(BlogAdminIndexSnapshot snapshot) {
        return new BlogAdminIndexChange(
            "BOOTSTRAP",
            snapshot.blog(),
            snapshot.draft(),
            snapshot.revision(),
            snapshot.publishedRevision());
    }

    public static BlogRevisionIndexEntry toRevision(BlogRevisionSnapshot revision) {
        return new BlogRevisionIndexEntry(
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
}
