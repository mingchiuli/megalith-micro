package wiki.chiu.micro.exhibit.application.model;

import java.time.LocalDateTime;

/**
 * A blog as it appears in a page listing.
 */
public record BlogDescription(
    Long id, String title, String description, Integer status, LocalDateTime created, String link) {

    public BlogDescription withMasked(String title, String description) {
        return new BlogDescription(id, title, description, status, created, link);
    }
}
