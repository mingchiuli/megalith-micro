package wiki.chiu.micro.blog.api.vo;

import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public record BlogPublicationContentRpcVo(
    String title,
    String description,
    String content,
    BlogRenderMode renderMode,
    List<BlogSensitiveSpan> sensitiveContent) {
}
