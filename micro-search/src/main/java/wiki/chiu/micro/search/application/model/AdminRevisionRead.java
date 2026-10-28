package wiki.chiu.micro.search.application.model;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.enums.BlogRevisionOrigin;

public record AdminRevisionRead(
    Long id,
    Long blogId,
    Long versionNo,
    BlogRevisionOrigin origin,
    Long baseRevisionId,
    String title,
    LocalDateTime firstPublishedAt,
    Long createdBy,
    LocalDateTime createdAt,
    boolean currentPublished) {
}
