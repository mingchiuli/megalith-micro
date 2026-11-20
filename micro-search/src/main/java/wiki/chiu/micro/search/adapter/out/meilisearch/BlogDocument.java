package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record BlogDocument(
    Long id,
    Long userId,
    Integer status,
    Long readCount,
    Long revision,
    Long publishedRevisionId,
    Long versionNo,
    Integer accessMode,
    BlogRenderMode renderMode,
    LocalDateTime publishedAt,
    LocalDateTime offlineAt,
    List<BlogSensitiveSpan> sensitiveContent,
    Boolean deleted,
    String title,
    String description,
    String content,
    String link,
    LocalDateTime created,
    LocalDateTime updated) {
}
