package wiki.chiu.micro.common.model;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;

public record BlogDraftSnapshot(
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
}
