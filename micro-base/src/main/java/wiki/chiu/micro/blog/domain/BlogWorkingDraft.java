package wiki.chiu.micro.blog.domain;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record BlogWorkingDraft(
    Long id,
    Long blogId,
    Long draftRevision,
    Long basePublishedRevisionId,
    String title,
    String description,
    String content,
    String link,
    BlogRenderMode renderMode,
    Long updatedBy,
    LocalDateTime updatedAt,
    List<BlogSensitiveSpan> sensitiveContent) {

    public boolean newerThan(BlogIdentity blog) {
        return basePublishedRevisionId == null
            || (blog.publishedRevisionId() != null
                && basePublishedRevisionId.equals(blog.publishedRevisionId())
                && blog.publishedAt() != null
                && updatedAt.isAfter(blog.publishedAt()));
    }
}
