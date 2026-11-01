package wiki.chiu.micro.search.application.model;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record BlogRevisionIndexEntry(
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
