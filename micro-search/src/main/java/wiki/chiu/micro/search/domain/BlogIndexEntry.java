package wiki.chiu.micro.search.domain;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record BlogIndexEntry(
    Long id,
    Long userId,
    Integer status,
    Long readCount,
    String title,
    String description,
    String content,
    String link,
    LocalDateTime created,
    LocalDateTime updated,
    Long publishedRevisionId,
    Long versionNo,
    LocalDateTime publishedAt,
    LocalDateTime offlineAt,
    Integer accessMode,
    BlogRenderMode renderMode,
    List<BlogSensitiveSpan> sensitiveContent) {

    public BlogIndexEntry(
        Long id,
        Long userId,
        Integer status,
        Long readCount,
        String title,
        String description,
        String content,
        LocalDateTime created,
        LocalDateTime updated) {
        this(
            id,
            userId,
            status,
            readCount,
            title,
            description,
            content,
            null,
            created,
            updated,
            null,
            null,
            null,
            null,
            null,
            BlogRenderMode.NORMAL,
            List.of());
    }
}
