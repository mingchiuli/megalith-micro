package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;

/**
 * A blog in the recycle bin, numbered by its position in the list.
 */
public record DeletedBlogItem(
    Long id,
    Long userId,
    String title,
    String description,
    String content,
    LocalDateTime created,
    LocalDateTime updated,
    Integer status,
    Integer idx,
    String link,
    Long readCount) {
}
