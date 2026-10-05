package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;

/**
 * A blog as it appears in the administration list, with the recent read count beside the total.
 */
public record BlogListItem(
    Long id,
    String title,
    String description,
    String content,
    String link,
    Long readCount,
    Integer recentReadCount,
    LocalDateTime created,
    LocalDateTime updated,
    Integer status) {
}
