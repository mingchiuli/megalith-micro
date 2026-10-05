package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;

/**
 * The export filters a client submits.
 */
public record BlogDownloadQuery(
    String keywords, Integer status, LocalDateTime createStart, LocalDateTime createEnd) {
}
