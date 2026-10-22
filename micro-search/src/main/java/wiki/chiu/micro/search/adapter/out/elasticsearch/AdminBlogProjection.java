package wiki.chiu.micro.search.adapter.out.elasticsearch;

import java.time.ZonedDateTime;

import wiki.chiu.micro.common.enums.BlogRenderMode;

public record AdminBlogProjection(
    Long id,
    Long userId,
    Long publishedRevisionId,
    Long publicationRevision,
    Long adminRevision,
    Long readCount,
    ZonedDateTime created,
    ZonedDateTime publishedAt,
    ZonedDateTime offlineAt,
    Integer accessMode,
    BlogRenderMode renderMode,
    Long draftRevision,
    Long basePublishedRevisionId,
    boolean hasDraft,
    boolean draftNewer,
    ZonedDateTime draftUpdatedAt,
    String publishedTitle,
    String publishedDescription,
    String publishedContent,
    String publishedLink,
    String draftTitle,
    String draftDescription,
    String draftContent,
    String draftLink,
    Integer status,
    boolean deleted) {
}
