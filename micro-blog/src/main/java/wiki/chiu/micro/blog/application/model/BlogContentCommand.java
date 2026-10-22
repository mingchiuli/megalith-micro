package wiki.chiu.micro.blog.application.model;

import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record BlogContentCommand(
    String title,
    String description,
    String content,
    String link,
    BlogRenderMode renderMode,
    List<BlogSensitiveSpan> sensitiveContent) {
}
