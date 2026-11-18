package wiki.chiu.micro.blog.api.vo;

import java.time.LocalDateTime;

import wiki.chiu.micro.common.enums.BlogAccessMode;

public record BlogPublicationRpcVo(
    Long revisionId,
    Long userId,
    BlogAccessMode accessMode,
    LocalDateTime created,
    LocalDateTime publishedAt,
    Long readCount) {
}
