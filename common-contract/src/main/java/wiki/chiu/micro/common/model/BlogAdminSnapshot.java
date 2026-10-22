package wiki.chiu.micro.common.model;

import java.time.LocalDateTime;

public record BlogAdminSnapshot(
    Long id,
    Long userId,
    Long publishedRevisionId,
    LocalDateTime publishedAt,
    LocalDateTime offlineAt,
    Integer accessMode,
    Long publicationRevision,
    Long adminRevision,
    Long readCount,
    LocalDateTime createdAt) {
}
