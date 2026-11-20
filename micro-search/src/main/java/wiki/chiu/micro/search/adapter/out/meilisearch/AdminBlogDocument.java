package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.time.LocalDateTime;
import java.time.ZoneId;

import wiki.chiu.micro.common.enums.BlogRenderMode;

public record AdminBlogDocument(
    Long id,
    Long userId,
    Long publishedRevisionId,
    Long publicationRevision,
    Long adminRevision,
    Long readCount,
    LocalDateTime created,
    LocalDateTime publishedAt,
    LocalDateTime offlineAt,
    Integer accessMode,
    BlogRenderMode renderMode,
    Long draftRevision,
    Long basePublishedRevisionId,
    boolean hasDraft,
    boolean draftNewer,
    LocalDateTime draftUpdatedAt,
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

    public String getOwnerKey() {
        return "u" + userId;
    }

    public Long getCreatedMillis() {
        return created == null ? null : created.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
    }
}
