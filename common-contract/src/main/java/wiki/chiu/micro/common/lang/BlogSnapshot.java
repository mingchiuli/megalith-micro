package wiki.chiu.micro.common.lang;

import java.time.LocalDateTime;

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
    Long revision) {
}
