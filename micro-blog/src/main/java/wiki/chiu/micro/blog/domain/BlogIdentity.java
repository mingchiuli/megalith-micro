package wiki.chiu.micro.blog.domain;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.enums.BlogAccessMode;

public record BlogIdentity(
    Long id,
    Long userId,
    LocalDateTime created,
    Long readCount,
    Long publishedRevisionId,
    LocalDateTime publishedAt,
    LocalDateTime offlineAt,
    BlogAccessMode accessMode,
    Long publicationRevision,
    Long adminRevision) {

    public boolean published() {
        return publishedRevisionId != null;
    }

    public boolean online() {
        return published() && offlineAt == null;
    }
}
