package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;

/**
 * The filters applied when searching for blog entries.
 */
public record BlogSearchSelection(
    Integer status, LocalDateTime createStart, LocalDateTime createEnd, Long userId, boolean allData) {
}
