package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;

/**
 * The blog list filters a client submits.
 */
public record BlogQuery(
    Integer currentPage,
    Integer size,
    String keywords,
    Integer status,
    LocalDateTime createStart,
    LocalDateTime createEnd) {
}
