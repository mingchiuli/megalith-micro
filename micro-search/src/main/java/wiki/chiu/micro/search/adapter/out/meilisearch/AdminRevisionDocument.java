package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;

public record AdminRevisionDocument(
    Long id,
    Long blogId,
    Long versionNo,
    BlogRevisionOrigin origin,
    Long baseRevisionId,
    String title,
    String description,
    String content,
    String link,
    BlogRenderMode renderMode,
    LocalDateTime firstPublishedAt,
    Long createdBy,
    LocalDateTime createdAt) {

    public String getArticleKey() {
        return "b" + blogId;
    }

    public String getOwnerKey() {
        return "u" + createdBy;
    }
}
