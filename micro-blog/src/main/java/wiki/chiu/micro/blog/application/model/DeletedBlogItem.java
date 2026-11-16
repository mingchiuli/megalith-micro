package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;

/**
 * A blog in the recycle bin, identified by its stable recovery identifier.
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
    String recoveryId,
    String link,
    Long readCount) {
}
