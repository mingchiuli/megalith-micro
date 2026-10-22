package wiki.chiu.micro.common.model;

import java.time.LocalDateTime;
import java.util.List;

public record BlogSnapshot(
    Long id,
    Long userId,
    String title,
    String description,
    String content,
    LocalDateTime created,
    LocalDateTime updated,
    Integer status,
    String link,
    Long readCount,
    Long revision,
    Long publishedRevisionId,
    LocalDateTime publishedAt,
    LocalDateTime offlineAt,
    Integer accessMode,
    Integer renderMode,
    List<BlogSensitiveSpan> sensitiveContent) {

    public BlogSnapshot(
        Long id,
        Long userId,
        String title,
        String description,
        String content,
        LocalDateTime created,
        LocalDateTime updated,
        Integer status,
        String link,
        Long readCount,
        Long revision) {
        this(
            id,
            userId,
            title,
            description,
            content,
            created,
            updated,
            status,
            link,
            readCount,
            revision,
            null,
            null,
            null,
            null,
            null,
            List.of());
    }
}
