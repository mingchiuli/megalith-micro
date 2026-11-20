package wiki.chiu.micro.search.api.vo;

import java.time.LocalDateTime;
import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record PublicBlogRpcVo(
    Long id,
    String title,
    String description,
    String link,
    Long readCount,
    LocalDateTime created,
    LocalDateTime publishedAt,
    BlogRenderMode renderMode,
    List<BlogSensitiveSpan> sensitiveContent) {

    public PublicBlogRpcVo {
        sensitiveContent = sensitiveContent == null ? List.of() : List.copyOf(sensitiveContent);
    }
}
