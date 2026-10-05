package wiki.chiu.micro.exhibit.application.model;

import java.time.LocalDateTime;

/**
 * A blog as it appears on its own detail page, already joined with its author.
 */
public record BlogExhibit(
    Long userId,
    String description,
    String nickname,
    String avatar,
    String title,
    String content,
    LocalDateTime created,
    Long readCount,
    Integer status) {

    public BlogExhibit withMasked(String title, String description, String content) {
        return new BlogExhibit(
            userId, description, nickname, avatar, title, content, created, readCount, status);
    }
}
