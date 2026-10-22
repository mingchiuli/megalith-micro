package wiki.chiu.micro.search.application.model;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record PublicBlogRead(
    Long id,
    Long userId,
    Long publishedRevisionId,
    Long versionNo,
    String title,
    String description,
    String content,
    String link,
    Long readCount,
    LocalDateTime created,
    LocalDateTime publishedAt,
    LocalDateTime offlineAt,
    Integer accessMode,
    BlogRenderMode renderMode,
    List<BlogSensitiveSpan> sensitiveContent) {
}
