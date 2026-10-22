package wiki.chiu.micro.exhibit.application.model;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.model.BlogSensitiveSpan;

/**
 * A blog as it appears in a page listing.
 */
public record BlogDescription(
    Long id,
    String title,
    String description,
    Integer status,
    LocalDateTime created,
    String link,
    List<BlogSensitiveSpan> sensitiveContent) {

    public BlogDescription(
        Long id,
        String title,
        String description,
        Integer status,
        LocalDateTime created,
        String link) {
        this(id, title, description, status, created, link, List.of());
    }

    public BlogDescription withMasked(String title, String description) {
        return new BlogDescription(id, title, description, status, created, link, sensitiveContent);
    }
}
