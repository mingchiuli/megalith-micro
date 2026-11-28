package wiki.chiu.micro.blog.adapter.in.http;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.enums.BlogRevisionOrigin;

public record BlogVersionSummaryVo(
    Long id,
    Long blogId,
    Long versionNo,
    BlogRevisionOrigin origin,
    Long baseRevisionId,
    String title,
    LocalDateTime firstPublishedAt,
    Long createdBy,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
    boolean currentPublished) {
}
