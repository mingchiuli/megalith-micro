package wiki.chiu.micro.search.api.vo;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record PublicBlogRpcVo(
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
