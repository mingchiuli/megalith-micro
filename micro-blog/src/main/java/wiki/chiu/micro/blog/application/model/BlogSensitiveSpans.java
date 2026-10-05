package wiki.chiu.micro.blog.application.model;

import java.util.List;

/**
 * The spans marked on one blog.
 */
public record BlogSensitiveSpans(Long blogId, List<SensitiveContentSpan> sensitiveContent) {

    public BlogSensitiveSpans {
        sensitiveContent = List.copyOf(sensitiveContent);
    }
}
