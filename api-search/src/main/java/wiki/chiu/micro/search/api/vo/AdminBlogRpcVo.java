package wiki.chiu.micro.search.api.vo;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.enums.BlogRenderMode;

public record AdminBlogRpcVo(
    Long id,
    Long userId,
    Long publishedRevisionId,
    Long publicationRevision,
    Long adminRevision,
    String title,
    String description,
    String content,
    String link,
    Long readCount,
    LocalDateTime created,
    LocalDateTime updated,
    Integer status,
    Integer accessMode,
    BlogRenderMode renderMode,
    boolean hasDraft,
    boolean draftNewer,
    Long draftRevision,
    Long basePublishedRevisionId,
    LocalDateTime draftUpdatedAt) {
}
