package wiki.chiu.micro.common.model;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;

public record BlogRevisionSnapshot(
    Long id,
    Long blogId,
    Long versionNo,
    BlogRevisionOrigin origin,
    Long baseRevisionId,
    String title,
    String description,
    String content,
    String link,
    BlogRenderMode renderMode,
    LocalDateTime firstPublishedAt,
    Long createdBy,
    LocalDateTime createdAt,
    List<BlogSensitiveSpan> sensitiveContent) {
}
