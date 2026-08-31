package wiki.chiu.micro.search.domain;

import java.time.LocalDateTime;

public record BlogIndexEntry(
    Long id,
    Long userId,
    Integer status,
    Long readCount,
    String title,
    String description,
    String content,
    LocalDateTime created,
    LocalDateTime updated) {
}
